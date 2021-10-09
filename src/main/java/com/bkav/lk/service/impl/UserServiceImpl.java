package com.bkav.lk.service.impl;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.dto.UserHistoryDTO;
import com.bkav.lk.repository.*;
import com.bkav.lk.security.AuthoritiesConstants;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.UserMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AuthorityRepository authorityRepository;

    private final UserRepository userRepository;

    private final ActivityLogRepository activityLogRepository;

    private final PositionRepository positionRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserExternalAuthRepository userExternalAuthRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthorityRepository authorityRepository,
                           ActivityLogRepository activityLogRepository, PositionRepository positionRepository, UserMapper userMapper, UserExternalAuthRepository userExternalAuthRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.activityLogRepository = activityLogRepository;
        this.positionRepository = positionRepository;
        this.userMapper = userMapper;
        this.userExternalAuthRepository = userExternalAuthRepository;
    }

    @Override
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        } else {
            user.setName("");
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        } else {
            user.setPhoneNumber("");
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        if (userDTO.getAvatar() != null) {
            user.setAvatar(userDTO.getAvatar());
        }
        user.setActivated(true);
        user.setStatus(Constants.ENTITY_STATUS.ACTIVE);

        Set<Authority> authorities;
        if (userDTO.getAuthorities() != null) {
            authorities = userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        } else {
            authorities = new HashSet<>();
            Authority authority = new Authority();
            authority.setName(AuthoritiesConstants.USER);
            authorities.add(authority);
        }
        user.setAuthorities(authorities);
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    @Override
    public User getCurrentUser() {
        Optional<String> optionalLogin = SecurityUtils.getCurrentUserLogin();
        if (!optionalLogin.isPresent()) {
            return null;
        }
        Optional<User> optionalUser = findByLogin(optionalLogin.get());
        return optionalUser.orElse(null);
    }

    @Override
    public Optional<User> findOne(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Page<UserDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<UserDTO> userDTOList = userMapper.toDto(userRepository.search(queryParams, pageable));
        return new PageImpl<>(userDTOList, pageable, userRepository.count(queryParams));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            return userRepository.findOneWithAuthoritiesByLoginIgnoreCaseAndStatusIs(currentUserLogin.get(),
                    Constants.ENTITY_STATUS.ACTIVE);
        }
        return Optional.empty();

    }

    @Override
    public List<User> getListUser() {
        return userRepository.getListUser();
    }

    @Override
    public List<UserHistoryDTO> getUserHistoryById(Long id) {
        List<UserHistoryDTO> listUserHistory = new ArrayList<>();
        List<ActivityLog> listActivityLog = activityLogRepository.findByContentIdAndContentTypeOrderByCreatedDateDesc(id, Constants.CONTENT_TYPE.USER);
        for (ActivityLog activityLog : listActivityLog) {
            UserHistoryDTO history = new UserHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            List<String> newContentList = new ArrayList<>();
            List<String> oldContentList = new ArrayList<>();
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                newContentList.add("Thêm mới");
                history.setNewContents(newContentList);
                listUserHistory.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                User oldUser = convertToUser(activityLog.getOldContent());
                User newUser = convertToUser(activityLog.getContent());
                createContentList(oldUser, newUser, oldContentList, newContentList);
            }
            // For situation when Updating User but not change anything
            if (oldContentList.size() == 0 || newContentList.size() == 0) {
                continue;
            }
            history.setOldContents(oldContentList);
            history.setNewContents(newContentList);
            listUserHistory.add(history);
        }

        return listUserHistory;
    }

    private User convertToUser(String input) {
        if (input.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(input, User.class);
    }

    private void createContentList(User oldUser, User newUser, List<String> oldContentList, List<String> newContentList) {
        if (oldUser == null) {
            return;
        }
        if (!oldUser.getName().equals(newUser.getName())) {
            oldContentList.add("Tên người dùng: " + oldUser.getName());
            newContentList.add("Tên người dùng: " + newUser.getName());
        }
        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            oldContentList.add("Email: " + oldUser.getEmail());
            newContentList.add("Email: " + newUser.getEmail());
        }
        if (!oldUser.getPhoneNumber().equals(newUser.getPhoneNumber())) {
            oldContentList.add("Số điện thoại: " + oldUser.getPhoneNumber());
            newContentList.add("Số điện thoại: " + newUser.getPhoneNumber());
        }
        Position oldPosition = oldUser.getPosition();
        Position newPosition = newUser.getPosition();
        if ((oldPosition == null && newPosition != null) ||
                (oldPosition != null && newPosition == null) ||
                (oldPosition != null && !oldPosition.getId().equals(newPosition.getId()))) {
            oldContentList.add(createPositionString(oldPosition));
            newContentList.add(createPositionString(newPosition));
        }

        if (compareGroups(oldUser.getGroups(), newUser.getGroups())) {
            oldContentList.add(createGroupsString(oldUser.getGroups()));
            newContentList.add(createGroupsString(newUser.getGroups()));
        }
        if (!oldUser.getStatus().equals(newUser.getStatus())) {
            oldContentList.add("Trạng thái: " +
                    (oldUser.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
            newContentList.add("Trạng thái: " +
                    (newUser.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
        }
    }

    private String createPositionString(Position position) {
        String str = "Mã chức vụ: ";
        if (position == null) {
            return str;
        }
        return str + positionRepository.getOne(position.getId()).getCode();
    }

    private boolean compareGroups(Set<Group> oldGroups, Set<Group> newGroups) {
        if (oldGroups.size() == 0 && newGroups.size() != 0) {
            return true;
        }
        if (oldGroups.size() != 0 && newGroups.size() == 0) {
            return true;
        }
        List<Long> oldGroupIds = oldGroups.stream().map(Group::getId).sorted().collect(Collectors.toList());
        List<Long> newGroupIds = newGroups.stream().map(Group::getId).sorted().collect(Collectors.toList());
        return !oldGroupIds.equals(newGroupIds);
    }

    private String createGroupsString(Set<Group> setGroup) {
        StringBuilder str = new StringBuilder("Nhóm quyền: ");
        if (setGroup.size() == 0) {
            return str.toString();
        }
        for (Group group : setGroup) {
            str.append(group.getGroupName()).append(", ");
        }
        return str.substring(0, str.length() - 2);
    }

    @Override
    public User update(UserDTO userDTO) {
        Optional<User> optional = userRepository.findById(userDTO.getId());
        if (!optional.isPresent()) {
            return null;
        }
        User user = setPropertyOfUser(optional.get(), userDTO);
        userRepository.save(user);
        return user;
    }

    @Override
    public User updateByMobile(User user) {
        return userRepository.save(user);
    }

    @Override
    public Long countActiveAccount() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("status", Constants.ENTITY_STATUS.ACTIVE.toString());
        return userRepository.count(map);
    }

    @Override
    public List<User> findByGroup(Long groupId) {
        return userRepository.findByGroups_Id(groupId);
    }

    @Override
    public void deleteAll(List<UserDTO> userDTOs) {
        userDTOs.forEach(o -> o.setStatus(Constants.ENTITY_STATUS.DELETED));
        userRepository.saveAll(userDTOs.stream().map(userMapper::toEntity).collect(Collectors.toList()));
    }

    @Override
    public boolean existsByPositionId(Long positionId) {
        return userRepository.existsByPositionIdAndStatus(positionId, Constants.ENTITY_STATUS.ACTIVE);
    }

    private User setPropertyOfUser(User user, UserDTO userDTO) {
        user.setName(userDTO.getName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEmail(userDTO.getEmail());
        user.setHealthFacilityId(userDTO.getHealthFacilityId());
        if (userDTO.getPositionId() != null) {
            Position position = new Position();
            position.setId(userDTO.getPositionId());
            user.setPosition(position);
        }
        if (userDTO.getDepartmentId() != null) {
            Department department = new Department();
            department.setId(userDTO.getDepartmentId());
            user.setDepartment(department);
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
        user.setDoctorId(userDTO.getDoctorId());
        user.setGroups(userDTO.getGroups());
        return user;
    }

    @Override
    public List<User> findByDepartment(Long departmentId, Integer[] status) {
        List<User> users = userRepository.findByDepartment_IdAndStatusIn(departmentId, status);
        return users;
    }

    @Override
    public User authenticationSocialUser(UserDTO userDTO) {
        User user = null;
        if (userDTO.getSocialId() != null) {
            Optional<UserExternalAuth> userExternalLoginOptional =
                    userExternalAuthRepository.getFirstByTypeAndAndExternalUserIdAndStatusIs(
                            userDTO.getSocialType(),
                            userDTO.getSocialId(),
                            Constants.ENTITY_STATUS.ACTIVE);
            Optional<User> userOptional;
            if (userExternalLoginOptional.isPresent()) {
                userOptional = userRepository.findById(userExternalLoginOptional.get().getUserId());
            } else {
                userOptional = Optional.empty();
            }
            if (userOptional.isPresent()
                    && Constants.ENTITY_STATUS.ACTIVE.equals(userOptional.get().getStatus())) {
                user = userOptional.get();
            } else {
                UserExternalAuth userExternalAuth = new UserExternalAuth();
                if (!StrUtil.isBlank(userDTO.getEmail())) {
                    userOptional = userRepository.findOneWithAuthoritiesByEmailIgnoreCase(userDTO.getEmail());
                }
                if (userOptional.isPresent()) {
                    userExternalAuth.setUserId(userOptional.get().getId());
                    user = userOptional.get();
                } else {
                    String login = userDTO.getLogin(), temp = userDTO.getLogin();
                    boolean existed = userRepository.existsByLogin(login);
                    int count = 1;
                    while (existed) {
                        temp = String.format("%s%s", login, count++);
                        existed = userRepository.existsByLogin(temp);
                    }
                    if (!temp.equals(login)) userDTO.setLogin(temp);
                    userDTO.setActivated(true);
                    userDTO.setPassword(Constants.DEFAULT_SECRET.PASS_WORD);
                    // SECRET PW = 12345678
                    user = createUser(userDTO);
                    userExternalAuth.setUserId(user.getId());
                }
                userExternalAuth.setExternalUserId(userDTO.getSocialId());
                userExternalAuth.setType(userDTO.getSocialType());
                userExternalAuth.setStatus(Constants.ENTITY_STATUS.ACTIVE);
                userExternalAuthRepository.save(userExternalAuth);
            }
        }
        userRepository.save(user);
        return user;
    }

    @Override
    public List<UserDTO> findByAreaCode(String code) {
        List<User> results = null;
        if (code.equals("0")) {
            results = userRepository.findByCityAreaCode("15");
        } else {
            results = userRepository.findByDistrictAreaCode(code);
        }
        return userMapper.toDto(results);
    }

    @Override
    public List<UserDTO> getListUserIsAssignedDoctors(Long healthFacilityId) {
        List<User> userDTOList = userRepository.findAllByHealthFacilityIdAndDoctorIdNotNull(healthFacilityId);
        return userMapper.toDto(userDTOList);
    }
}

package com.bkav.lk.web.rest;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.dto.UserHistoryDTO;
import com.bkav.lk.repository.AuthorityRepository;
import com.bkav.lk.service.*;
import com.bkav.lk.service.mapper.UserMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserResource {
    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    private static final String ENTITY_NAME = "userManagement";

    private static final String HEADER_X_TOTAL_ACTIVE_COUNT = "X-Total-Active-Count";
    private final UserService userService;
    private final AuthorityRepository authorityRepository;

    private final ActivityLogService activityLogService;

    private final AppointmentCancelConfigService appointmentCancelConfigService;

    private final ConfigService configService;

    private final UploadedFileService uploadedFileService;

    private final UserMapper userMapper;

    @Value("${spring.application.name}")
    private String applicationName;

    public UserResource(UserService userService, AuthorityRepository authorityRepository,
                        ActivityLogService activityLogService, AppointmentCancelConfigService appointmentCancelConfigService, ConfigService configService, UploadedFileService uploadedFileService, UserMapper userMapper) {
        this.userService = userService;
        this.authorityRepository = authorityRepository;
        this.activityLogService = activityLogService;
        this.appointmentCancelConfigService = appointmentCancelConfigService;
        this.configService = configService;
        this.uploadedFileService = uploadedFileService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getListUser() {
        List<User> listUser = userService.getListUser();
        return ResponseEntity.ok(listUser);
    }

    @GetMapping("/users/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = userService.getCurrentUser();
        user.setPassword("");
        return ResponseEntity.ok().body(userMapper.toDto(user));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                Pageable pageable) {
        Page<UserDTO> page = userService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequestUri(), page);

        // only count active user for default
        if (queryParams.containsKey("isDefault") && queryParams.get("isDefault").get(0).equals("true")) {
            headers.add(HEADER_X_TOTAL_ACTIVE_COUNT, userService.countActiveAccount().toString());
        }
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO userDTO) {
        if (userDTO.getId() != null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "user", "", "A new User cannot already have an Id"))
                    .body(null);
        }
        if (userDTO.getLogin() == null) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "user", "null.login", "Empty Login"))
                    .body(null);
        }
        if (isLoginDuplicated(userDTO.getLogin())) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "user", "duplicate.login", "Duplicate Login"))
                    .body(null);
        }
        if (isEmailDuplicated(userDTO.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "user", "duplicate.email", "Duplicate Email"))
                    .body(null);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .headers(HeaderUtil.createFailureAlert("", true, "user", "empty.password", "Empty Password"))
                    .body(null);
        }
        User result = userService.createUser(userDTO);
        return ResponseEntity.ok().body(userMapper.toDto(result));
    }

    private boolean isLoginDuplicated(String newLogin) {
        Optional<User> user = userService.findByLogin(newLogin);
        return user.isPresent();
    }

    private boolean isEmailDuplicated(String newEmail) {
        if (newEmail.isEmpty()) {
            return false;
        }
        Optional<User> user = userService.findByEmail(newEmail);
        return user.isPresent();
    }


    @PutMapping("/users")
    public ResponseEntity<Boolean> update(@RequestBody UserDTO userDTO) {
        if (userDTO.getLogin() == null) {
            throw new BadRequestAlertException("Invalid login", ENTITY_NAME, "loginnull");
        }
        User oldUser = new User();
        Optional<User> optional = userService.findOne(userDTO.getId());
        if (optional.isPresent()) {
            oldUser = optional.get();
            oldUser.setPassword("");
            Set<Group> oldGroups = new HashSet<>(oldUser.getGroups());
            oldUser.setGroups(oldGroups);
        }
        User result = userService.update(userDTO);
        result.setPassword("");
        activityLogService.update(Constants.CONTENT_TYPE.USER, oldUser, result);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userDTO.getLogin()))
                .build();
    }

    @PutMapping("/users/mobile/update")
    public ResponseEntity<UserDTO> updateByMobile
            (@RequestParam(name = "name") String name,
             @RequestParam(name = "dob") Instant dob,
             @RequestParam(name = "avatar", required = false) MultipartFile avatar) {
        User currentUser = userService.getCurrentUser();
        UserDTO oldUserDTO = userMapper.toDto(currentUser);
        currentUser.setName(name);
        currentUser.setDob(dob);

        UploadedFile uploadedFile;
        if (avatar != null) {
            String originalName = StringUtils.cleanPath(avatar.getOriginalFilename());
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            if (!extension.equalsIgnoreCase(".jpeg") && !extension.equalsIgnoreCase(".jpg")
                    && !extension.equalsIgnoreCase(".png")) {
                throw new BadRequestAlertException("Invalid File Image", ENTITY_NAME, "invalidImage");
            }
            try {
                uploadedFile = uploadedFileService.store(avatar);
                currentUser.setAvatar(uploadedFile.getStoredName());
            } catch (IOException ignored) {
            }
        }

        User updatedUser = userService.updateByMobile(currentUser);
        currentUser.setPassword("");
        oldUserDTO.setPassword("");
        activityLogService.update(Constants.CONTENT_TYPE.USER, userMapper.toEntity(oldUserDTO), updatedUser);
        return ResponseEntity.ok().body(userMapper.toDto(updatedUser));
    }

    @GetMapping("/users/{id}/history")
    public ResponseEntity<List<UserHistoryDTO>> getUserHistory(@PathVariable Long id) {
        List<UserHistoryDTO> list = userService.getUserHistoryById(id);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/users/blocked")
    public ResponseEntity<Boolean> checkUserBlocked() {
        Boolean userHasBlocked = appointmentCancelConfigService.hasBlocked();
        return ResponseEntity.ok().body(userHasBlocked);
    }

    @GetMapping("/users/message-blocked")
    public ResponseEntity<Map<String, Object>> notifyContentUserIsBlock() {
        Map<String, Object> map = new HashMap<>();
        AppointmentCancelLog log = appointmentCancelConfigService.findByCurrentUser();
        String message = "";
        if (Objects.nonNull(log)) {
            if (log.getIsBlocked().equals(Constants.BOOL_NUMBER.FALSE)) {
                map.put("message", message);
                map.put("isBlock", false);
            } else {
                Config exceedDay = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
                Config exceedWeek = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
                Config blockAccountDay = configService.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_IN_DAY);
                Integer maxDayCancel = log.getMaxDayCanceled();
                Integer maxWeekCancel = log.getMaxWeekCanceled();
                Instant date = log.getStartBlockedDate().plus(Integer.parseInt(blockAccountDay.getPropertyValue()), ChronoUnit.DAYS);
                List<String> params = new ArrayList<>();
                if (maxDayCancel.equals(0)) {
                    params.add(exceedDay.getPropertyValue());
                    params.add(DateUtils.formatInstantAsString(date, "dd-MM-YYYY"));
                    message = StrUtil.getStringFromBundle("notify_datetime_user_block_by_date", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, params);
                } else if (maxWeekCancel.equals(0)) {
                    params.add(exceedWeek.getPropertyValue());
                    params.add(DateUtils.formatInstantAsString(date, "dd-MM-YYYY"));
                    message = StrUtil.getStringFromBundle("notify_datetime_user_block_by_week", com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, params);
                }
                map.put("message", message);
                map.put("isBlock", true);
            }
        } else {
            map.put("message", message);
            map.put("isBlock", false);
        }
        return ResponseEntity.ok().body(map);
    }

}

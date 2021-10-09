package com.bkav.lk.service;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.dto.UserHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(UserDTO userDTO);

    User getCurrentUser();

    Optional<User> findOne(Long id);

    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    Page<UserDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable);

    Optional<User> getUserWithAuthorities();

    List<User> getListUser();

    List<UserHistoryDTO> getUserHistoryById (Long id);

    User update (UserDTO userDTO);

    User updateByMobile(User user);

    Long countActiveAccount();

    List<User> findByGroup(Long groupId);

    List<User> findByDepartment(Long departmentId, Integer[] status);

    void deleteAll(List<UserDTO> userDTOs);

    boolean existsByPositionId(Long positionId);

    User authenticationSocialUser(UserDTO userDTO);

    List<UserDTO> findByAreaCode(String targetCode);

    List<UserDTO> getListUserIsAssignedDoctors(Long healthFacilityId);
}

package com.bkav.lk.repository;

import com.bkav.lk.domain.User;
import com.bkav.lk.repository.custom.UserRepositoryCustom;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";
    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    List<User> findByLoginIgnoreCase(String login);

    Optional<User> findByLogin(String login);

    Optional<User> findByLoginIgnoreCaseAndStatusIs(String login, Integer status);

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByLoginIgnoreCaseAndStatusIs(String login, Integer status);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCaseAndStatusIs(String email, Integer status);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    List<User> findByGroups_Id(Long groupId);

    List<User> findByDepartment_IdAndStatusIn(Long departmentId, Integer[] status);

    boolean existsByPositionIdAndStatus(Long positionId, Integer status);

    boolean existsByLogin(String login);

    List<User> findByCityAreaCode(String areaCode);

    List<User> findByDistrictAreaCode(String areaCode);

    List<User> findAllByHealthFacilityIdAndDoctorIdNotNull(Long healthFacilityId);
}

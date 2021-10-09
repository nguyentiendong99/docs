package com.bkav.lk.repository;

import com.bkav.lk.domain.UserExternalAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserExternalAuthRepository extends JpaRepository<UserExternalAuth, Long> {
    Optional<UserExternalAuth> getFirstByTypeAndAndExternalUserIdAndStatusIs(String socialType,
                                                                             String externalId,
                                                                             Integer status);
}

package com.bkav.lk.repository;

import com.bkav.lk.domain.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByReferenceIdAndPhoneNumber(String refId, String phoneNumber);

}

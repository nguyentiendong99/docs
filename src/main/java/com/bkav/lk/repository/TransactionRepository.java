package com.bkav.lk.repository;

import com.bkav.lk.domain.Transaction;
import com.bkav.lk.repository.custom.TransactionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {
    List<Transaction> findAllByPaymentStatusNotIn(Integer[] paymentStatus);

    Transaction findTopByBookingCode(String bookingCode);

    Transaction findTopByBookingCodeOrderByLastModifiedDateDesc(String bookingCode);

    Transaction findTopByBookingCodeAndTypeCodeAndPaymentStatusOrderByLastModifiedDateDesc(String bookingCode, String typeCode, Integer paymentStatus);
}

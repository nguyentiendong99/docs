package com.bkav.lk.repository.impl;

import com.bkav.lk.repository.custom.TransactionRepositoryCustom;
import com.bkav.lk.util.Constants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepositoryCustom  {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deleteTempTransactions(List<String> bookingCodes) {
        String sql = "UPDATE Transaction SET paymentStatus =:paymentStatus, lastModifiedDate =:lastModifiedDate WHERE bookingCode IN (:bookingCodes)";
        entityManager.createQuery(sql)
                .setParameter("paymentStatus", Constants.PAYMENT_STATUS.DELETE_TRANSACTION_TEMP_INVALID)
                .setParameter("lastModifiedDate", Instant.now())
                .setParameter("bookingCodes", bookingCodes)
                .executeUpdate();
    }
}

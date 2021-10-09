package com.bkav.lk.service;

import com.bkav.lk.dto.TransactionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    TransactionDTO save(TransactionDTO transactionDTO);

    List<TransactionDTO> getAll();

    TransactionDTO findByBookCode(String bookingCode);

    TransactionDTO findTopByBookingCodeAndTypeCodeAndPaymentStatus(String bookingCode, String typeCode, Integer paymentStatus);

    void refund(String bookingCode);

    void refund(Long patientRecordId, String bookingCode, String content);

    void refund(TransactionDTO oldTransactionDTO, BigDecimal amount, String content);

    void deleteTempTransactions(List<String> bookingCodes);
}

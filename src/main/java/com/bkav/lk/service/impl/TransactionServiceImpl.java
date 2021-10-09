package com.bkav.lk.service.impl;

import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.domain.Transaction;
import com.bkav.lk.dto.TransactionDTO;
import com.bkav.lk.repository.PatientRecordRepository;
import com.bkav.lk.repository.TransactionRepository;
import com.bkav.lk.service.TransactionService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.TransactionMapper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final PatientRecordRepository patientRecordRepository;

    private final TransactionMapper transactionMapper;

    private final UserService userService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, PatientRecordRepository patientRecordRepository, TransactionMapper transactionMapper, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.patientRecordRepository = patientRecordRepository;
        this.transactionMapper = transactionMapper;
        this.userService = userService;
    }

    @Override
    public TransactionDTO save(TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.save(transactionMapper.toEntity(transactionDTO));
        return transactionMapper.toDto(transaction);
    }

    @Override
    public List<TransactionDTO> getAll() {
        Integer[] paymentStatus = {Constants.PAYMENT_STATUS.REQUEST, Constants.PAYMENT_STATUS.DELETE_TRANSACTION_TEMP_INVALID};
        List<Transaction> transactionList = transactionRepository.findAllByPaymentStatusNotIn(paymentStatus);
        return transactionMapper.toDto(transactionList);
    }

    @Override
    public TransactionDTO findByBookCode(String bookingCode) {
        Transaction transaction = transactionRepository.findTopByBookingCodeOrderByLastModifiedDateDesc(bookingCode);
        return transactionMapper.toDto(transaction);
    }

    @Override
    public TransactionDTO findTopByBookingCodeAndTypeCodeAndPaymentStatus(String bookingCode, String typeCode, Integer paymentStatus) {
        Transaction transaction = transactionRepository.findTopByBookingCodeAndTypeCodeAndPaymentStatusOrderByLastModifiedDateDesc(bookingCode, typeCode, paymentStatus);
        return transactionMapper.toDto(transaction);
    }

    @Override
    public void refund(String bookingCode) {
        TransactionDTO transactionDTO = findByBookCode(bookingCode);
        if (Objects.nonNull(transactionDTO)) {
            TransactionDTO transactionDTOnew = new TransactionDTO();
            transactionDTOnew.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
            transactionDTOnew.setTypeCode(Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
            transactionDTOnew.setBookingCode(transactionDTO.getBookingCode());
            transactionDTOnew.setTotalAmount(transactionDTO.getTotalAmount());
            transactionDTOnew.setPaymentMethod(transactionDTO.getPaymentMethod());
            transactionDTOnew.setHealthFacilityId(transactionDTO.getHealthFacilityId());
            transactionDTOnew.setUserId(userService.getUserWithAuthorities().get().getId());
            transactionDTOnew.setTransactionCode(Utils.generateCodeFromId(userService.getUserWithAuthorities().get().getId()));
            transactionRepository.save(transactionMapper.toEntity(transactionDTOnew));
        }
    }

    @Override
    public void refund(Long patientRecordId, String bookingCode, String content) { // Hoàn tiền khi hủy lịch
        TransactionDTO transactionDTO = findByBookCode(bookingCode);
        Optional<PatientRecord> patientRecordOptional = patientRecordRepository.findById(patientRecordId);
        if (Objects.nonNull(transactionDTO)) {
            TransactionDTO transactionDTOnew = new TransactionDTO();
            transactionDTOnew.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
            transactionDTOnew.setTypeCode(Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
            transactionDTOnew.setBookingCode(transactionDTO.getBookingCode());
            transactionDTOnew.setAmount(transactionDTO.getAmount());
            transactionDTOnew.setTotalAmount(transactionDTO.getTotalAmount());
            transactionDTOnew.setPaymentMethod(transactionDTO.getPaymentMethod());
            transactionDTOnew.setHealthFacilityId(transactionDTO.getHealthFacilityId());
            if (patientRecordOptional.isPresent()) {
                transactionDTOnew.setUserId(patientRecordOptional.get().getUserId());
                transactionDTOnew.setTransactionCode(Utils.generateCodeFromId(patientRecordOptional.get().getUserId()));
            } else {
                transactionDTOnew.setUserId(userService.getUserWithAuthorities().get().getId());
                transactionDTOnew.setTransactionCode(Utils.generateCodeFromId(userService.getUserWithAuthorities().get().getId()));
            }
            transactionDTOnew.setContent(content);
            transactionRepository.save(transactionMapper.toEntity(transactionDTOnew));
        }
    }

    @Override
    public void refund(TransactionDTO oldTransactionDTO, BigDecimal amount, String content) { // Hoàn tiền khi thanh toán dịch vụ khám mới
        TransactionDTO transactionDTOnew = new TransactionDTO();
        transactionDTOnew.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
        transactionDTOnew.setTypeCode(Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
        transactionDTOnew.setBookingCode(oldTransactionDTO.getBookingCode());
        transactionDTOnew.setAmount(amount);
        transactionDTOnew.setTotalAmount(amount);
        transactionDTOnew.setPaymentMethod(oldTransactionDTO.getPaymentMethod());
        transactionDTOnew.setHealthFacilityId(oldTransactionDTO.getHealthFacilityId());
        transactionDTOnew.setUserId(oldTransactionDTO.getUserId());
        transactionDTOnew.setTransactionCode(Utils.generateCodeFromId(oldTransactionDTO.getUserId()));
        transactionDTOnew.setContent(content);
        transactionRepository.save(transactionMapper.toEntity(transactionDTOnew));
    }

    @Override
    public void deleteTempTransactions(List<String> bookingCodes) {
        transactionRepository.deleteTempTransactions(bookingCodes);
    }
}

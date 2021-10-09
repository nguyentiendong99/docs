package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Transaction;
import com.bkav.lk.dto.TransactionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends EntityMapper<TransactionDTO, Transaction> {
    Transaction toEntity(TransactionDTO transactionDTO);

    TransactionDTO toDto(Transaction transaction);

    default Transaction fromId(Long id) {
        if(id == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(id);
        return transaction;
    }
}

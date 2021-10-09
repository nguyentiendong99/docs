package com.bkav.lk.repository.custom;

import java.util.List;

public interface TransactionRepositoryCustom {

    void deleteTempTransactions(List<String> bookingCodes);
}

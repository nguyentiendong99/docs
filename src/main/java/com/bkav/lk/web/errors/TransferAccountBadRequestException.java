package com.bkav.lk.web.errors;

public class TransferAccountBadRequestException extends BadRequestAlertException{

    public TransferAccountBadRequestException(String defaultMessage) {
        super(defaultMessage, null, null);
    }
}

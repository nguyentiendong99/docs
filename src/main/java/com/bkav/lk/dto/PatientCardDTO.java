package com.bkav.lk.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class PatientCardDTO {

    private Long id;

    private Long userId;

    private String cardNumber;

    private String name;

    private Instant supplyDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getSupplyDate() {
        return supplyDate;
    }

    public void setSupplyDate(Instant supplyDate) {
        this.supplyDate = supplyDate;
    }
}

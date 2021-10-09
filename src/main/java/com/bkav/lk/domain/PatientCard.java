package com.bkav.lk.domain;

import com.bkav.lk.web.errors.validation.PatientCardNumberConstraint;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "patient_card")
public class PatientCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @PatientCardNumberConstraint
    @NotEmpty
    @Column(name = "card_number")
    private String cardNumber;

    @NotEmpty
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "supply_date")
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

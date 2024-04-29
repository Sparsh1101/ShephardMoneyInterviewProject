package com.shepherdmoney.interviewproject.model;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private LocalDate date;

    private double balance;

    // Establishing Many-to-One relationship with CreditCard entity
    @ManyToOne(cascade = CascadeType.ALL)
    private CreditCard creditCard;

    public BalanceHistory(LocalDate date, double balance) {
        this.date = date;
        this.balance = balance;
    }

    public BalanceHistory(LocalDate date, double balance, CreditCard creditCard) {
        this.date = date;
        this.balance = balance;
        this.creditCard = creditCard;
    }
}

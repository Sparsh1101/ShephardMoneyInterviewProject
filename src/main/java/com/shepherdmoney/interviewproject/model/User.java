package com.shepherdmoney.interviewproject.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    // Storing user's credit cards as a list of credit cards. We can directly query credit cards by user
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<CreditCard> creditCards;
}

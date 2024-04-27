package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.BalanceHistoryRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class CreditCardController {

    // wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;

    // wire in User repository here (~1 line)
    @Autowired
    private UserRepository userRepository;

    // wire in Balance History repository here (~1 line)
    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @PostMapping("/credit-card")
    // Creating new credit card and adding to user
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        try {
            // Check if the user exists
            Optional<User> optionalUser = userRepository.findById(payload.getUserId());
            if (optionalUser.isPresent()) {
                Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(payload.getCardNumber());
                if (!optionalCreditCard.isPresent()) {
                    // Create a new CreditCard entity
                    CreditCard creditCard = new CreditCard();
                    creditCard.setIssuanceBank(payload.getCardIssuanceBank());
                    creditCard.setNumber(payload.getCardNumber());

                    // Associate the credit card with the user
                    User user = optionalUser.get();
                    creditCard.setOwner(user);

                    // Save the credit card
                    CreditCard savedCreditCard = creditCardRepository.save(creditCard);

                    // Return the ID of the saved credit card in a 200 OK response
                    return ResponseEntity.ok(savedCreditCard.getId());
                }
            }
            // Return 400 Bad Request if the user does not exist
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // If an exception occurs during credit card creation
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/credit-card:all")
    // Returning all credit cards of a user
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        try {
            // Get the user by ID
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                // Get all credit cards associated with the user
                List<CreditCard> creditCards = optionalUser.get().getCreditCards();

                // Convert CreditCard entities to CreditCardView DTOs
                List<CreditCardView> creditCardViews = creditCards.stream()
                        .map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber()))
                        .collect(Collectors.toList());

                // Return the list of credit cards in a 200 OK response
                return ResponseEntity.ok(creditCardViews);
            } else {
                // Return an empty list if the user does not exist
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
            }
        } catch (Exception e) {
            // If an exception occurs during getting credit cards for user
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/credit-card:user-id")
    // Returning user id given credit card number
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        try {
            // Find the credit card by its number
            Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(creditCardNumber);
            if (optionalCreditCard.isPresent()) {
                // Get the user ID associated with the credit card
                int userId = optionalCreditCard.get().getOwner().getId();
                // Return the user ID in a 200 OK response
                return ResponseEntity.ok(userId);
            } else {
                // Return 400 Bad Request if no user is associated with the credit card
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            // If an exception occurs during getting user ID for Credit Card
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/credit-card:update-balance")
    // Update Balance History
    public ResponseEntity<?> updateBalance(@RequestBody UpdateBalancePayload[] payload) {
        try {
            // Group the payload entries based on credit card number
            Map<String, List<UpdateBalancePayload>> groupedPayload = new HashMap<>();
            for (UpdateBalancePayload updatePayload : payload) {
                groupedPayload.computeIfAbsent(updatePayload.getCreditCardNumber(), k -> new ArrayList<>())
                        .add(updatePayload);
            }

            // Sort each group chronologically by balance date
            groupedPayload.forEach((creditCardNumber, payloadList) -> payloadList
                    .sort(Comparator.comparing(UpdateBalancePayload::getBalanceDate)));

            // Iterate through each payload to update balance history
            for (List<UpdateBalancePayload> group : groupedPayload.values()) {
                // Find the credit card by its number
                Optional<CreditCard> optionalCreditCard = creditCardRepository
                        .findByNumber(group.get(0).getCreditCardNumber());

                if (optionalCreditCard.isPresent()) {
                    CreditCard creditCard = optionalCreditCard.get();
                    for (UpdateBalancePayload updatePayload : group) {

                        // Create a new BalanceHistory entry
                        BalanceHistory balanceHistory = new BalanceHistory();
                        balanceHistory.setDate(updatePayload.getBalanceDate());
                        balanceHistory.setBalance(updatePayload.getBalanceAmount());

                        // Save the BalanceHistory entry to the database
                        balanceHistoryRepository.save(balanceHistory);

                        // Update balance history
                        creditCard.addBalanceHistory(balanceHistory);
                        creditCardRepository.save(creditCard);

                    }
                } else {
                    // Return 400 Bad Request if no credit card is associated with the given card
                    // number
                    return ResponseEntity.badRequest()
                            .body("Credit card with number " + group.get(0).getCreditCardNumber()
                                    + " does not exist.");
                }
            }
            // Return 200 OK if update is successful
            return ResponseEntity.ok().body("Update successful for given balance history payload");
        } catch (Exception e) {
            // If an exception occurs during updating balance history payload
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}

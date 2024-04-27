package com.shepherdmoney.interviewproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.shepherdmoney.interviewproject.controller.CreditCardController;
import com.shepherdmoney.interviewproject.controller.UserController;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.BalanceHistoryRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

@ExtendWith(MockitoExtension.class)
class InterviewProjectApplicationTests {

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private CreditCardController creditCardController;

    @SuppressWarnings("null")
    @Test
    public void testCreateUser_Success() {
        // Mock data
        CreateUserPayload payload = new CreateUserPayload();
        payload.setName("John Doe");
        payload.setEmail("john@example.com");

        User newUser = new User();
        newUser.setId(1);
        newUser.setName(payload.getName());
        newUser.setEmail(payload.getEmail());

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        ResponseEntity<Integer> response = userController.createUser(payload);

        assert (response.getStatusCode()).equals(HttpStatus.OK);
        assert (response.getBody()).equals(newUser.getId());
    }

    @Test
    public void testCreateUser_Exception() {
        // Mock data
        CreateUserPayload payload = new CreateUserPayload();
        payload.setName("John Doe");
        payload.setEmail("john@example.com");

        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException());

        ResponseEntity<Integer> response = userController.createUser(payload);

        assert (response.getStatusCode()).equals(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("null")
    @Test
    public void testDeleteUser_Success() {
        // Mock data
        int userId = 1;
        User userToDelete = new User();
        userToDelete.setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);

        ResponseEntity<String> response = userController.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
        assert (response.getStatusCode()).equals(HttpStatus.OK);
        assert (response.getBody()).equals("User with ID " + userId + " deleted successfully.");
    }

    @SuppressWarnings("null")
    @Test
    public void testDeleteUser_UserNotFound() {
        // Mock data
        int userId = 1;

        when(userRepository.existsById(userId)).thenReturn(false);

        ResponseEntity<String> response = userController.deleteUser(userId);

        verify(userRepository, never()).deleteById(userId);
        assert (response.getStatusCode()).equals(HttpStatus.BAD_REQUEST);
        assert (response.getBody()).equals("User with ID " + userId + " does not exist.");
    }

    @Test
    public void testAddCreditCardToUser_Success() {
        // Mock data
        AddCreditCardToUserPayload payload = new AddCreditCardToUserPayload();
        payload.setUserId(1);
        payload.setCardIssuanceBank("Test Bank");
        payload.setCardNumber("1234567890");

        User user = new User();
        user.setId(1);

        // Mock the userRepository to return the user when findById is called
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        // Mock the creditCardRepository to return an empty optional, indicating that
        // the credit card does not already exist
        when(creditCardRepository.findByNumber("1234567890")).thenReturn(Optional.empty());
        // Mock the save method of creditCardRepository to return a CreditCard object
        // with ID 1
        CreditCard creditCard = new CreditCard();
        creditCard.setId(1);
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(creditCard);

        // Invoke the controller method
        ResponseEntity<Integer> response = creditCardController.addCreditCardToUser(payload);

        // Verify that the response status code is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code is not OK");
        // Verify that the response body contains the ID of the saved credit card
        assertEquals(1, response.getBody(), "Saved credit card ID is incorrect");
    }

    @Test
    public void testAddCreditCardToUser_UserNotFound() {
        // Mock data
        AddCreditCardToUserPayload payload = new AddCreditCardToUserPayload();
        payload.setUserId(1);
        payload.setCardIssuanceBank("Test Bank");
        payload.setCardNumber("1234567890");

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = creditCardController.addCreditCardToUser(payload);

        assert (response.getStatusCode()).equals(HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("null")
    @Test
    public void testGetAllCardOfUser_Success() {
        // Mock data
        int userId = 1;
        User user = new User();
        user.setId(userId);

        CreditCard creditCard1 = new CreditCard();
        creditCard1.setIssuanceBank("Bank1");
        creditCard1.setNumber("1234567890");

        CreditCard creditCard2 = new CreditCard();
        creditCard2.setIssuanceBank("Bank2");
        creditCard2.setNumber("0987654321");

        List<CreditCard> creditCards = new ArrayList<>();
        creditCards.add(creditCard1);
        creditCards.add(creditCard2);

        user.setCreditCards(creditCards);

        // Mock userRepository to return user when findById is called
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Invoke the controller method
        ResponseEntity<List<CreditCardView>> response = creditCardController.getAllCardOfUser(userId);

        // Verify that the response status code is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code is not OK");

        // Verify that the response body contains the correct number of credit cards
        assertEquals(2, response.getBody().size(), "Number of credit cards is incorrect");

        // Verify that the response body contains the correct credit card details
        assertEquals("Bank1", response.getBody().get(0).getIssuanceBank(), "First credit card bank is incorrect");
        assertEquals("1234567890", response.getBody().get(0).getNumber(), "First credit card number is incorrect");
        assertEquals("Bank2", response.getBody().get(1).getIssuanceBank(), "Second credit card bank is incorrect");
        assertEquals("0987654321", response.getBody().get(1).getNumber(), "Second credit card number is incorrect");
    }

    @SuppressWarnings("null")
    @Test
    public void testGetAllCardOfUser_UserNotFound() {
        // Mock data
        int userId = 1;

        // Mock userRepository to return empty optional, indicating user not found
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Invoke the controller method
        ResponseEntity<List<CreditCardView>> response = creditCardController.getAllCardOfUser(userId);

        // Verify that the response status code is 400 BAD REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code is not BAD REQUEST");
        // Verify that the response body is an empty list
        assertEquals(0, response.getBody().size(), "Response body is not an empty list");
    }

    @Test
    public void testGetUserIdForCreditCard_Success() {
        // Mock data
        String creditCardNumber = "1234567890";
        int userId = 1;

        CreditCard creditCard = new CreditCard();
        User user = new User();
        user.setId(userId);
        creditCard.setOwner(user);

        // Mock creditCardRepository to return the credit card when findByNumber is
        // called
        when(creditCardRepository.findByNumber(creditCardNumber)).thenReturn(Optional.of(creditCard));

        // Invoke the controller method
        ResponseEntity<Integer> response = creditCardController.getUserIdForCreditCard(creditCardNumber);

        // Verify that the response status code is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code is not OK");

        // Verify that the response body contains the correct user ID
        assertEquals(userId, response.getBody(), "User ID is incorrect");
    }

    @Test
    public void testGetUserIdForCreditCard_CreditCardNotFound() {
        // Mock data
        String creditCardNumber = "1234567890";

        // Mock creditCardRepository to return an empty optional, indicating credit card
        // not found
        when(creditCardRepository.findByNumber(creditCardNumber)).thenReturn(Optional.empty());

        // Invoke the controller method
        ResponseEntity<Integer> response = creditCardController.getUserIdForCreditCard(creditCardNumber);

        // Verify that the response status code is 400 BAD REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code is not BAD REQUEST");

        // Verify that the response body is null
        assertEquals(null, response.getBody(), "Response body is not null");
    }

    @Test
    public void testUpdateBalance_Success() {
        // Mock data
        String creditCardNumber = "1234567890";
        LocalDate balanceDate = LocalDate.now();
        double balanceAmount = 100.0;

        UpdateBalancePayload payload = new UpdateBalancePayload();
        payload.setCreditCardNumber(creditCardNumber);
        payload.setBalanceDate(balanceDate);
        payload.setBalanceAmount(balanceAmount);

        CreditCard creditCard = new CreditCard();
        BalanceHistory balanceHistory = new BalanceHistory(balanceDate, balanceAmount);

        // Mock creditCardRepository to return the credit card when findByNumber is
        // called
        when(creditCardRepository.findByNumber(creditCardNumber)).thenReturn(Optional.of(creditCard));
        // Mock balanceHistoryRepository to return the saved balance history
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenReturn(balanceHistory);

        // Invoke the controller method
        ResponseEntity<?> response = creditCardController.updateBalance(new UpdateBalancePayload[] { payload });

        // Verify that the response status code is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code is not OK");
    }

    @Test
    public void testUpdateBalance_CreditCardNotFound() {
        // Mock data
        String creditCardNumber = "1234567890";
        LocalDate balanceDate = LocalDate.now();
        double balanceAmount = 100.0;

        UpdateBalancePayload payload = new UpdateBalancePayload();
        payload.setCreditCardNumber(creditCardNumber);
        payload.setBalanceDate(balanceDate);
        payload.setBalanceAmount(balanceAmount);

        // Mock creditCardRepository to return an empty optional, indicating credit card
        // not found
        when(creditCardRepository.findByNumber(creditCardNumber)).thenReturn(Optional.empty());

        // Invoke the controller method
        ResponseEntity<?> response = creditCardController.updateBalance(new UpdateBalancePayload[] { payload });

        // Verify that the response status code is 400 BAD REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code is not BAD REQUEST");
    }

    @Test
    public void testUpdateBalance_Exception() {
        // Mock data
        String creditCardNumber = "1234567890";
        LocalDate balanceDate = LocalDate.now();
        double balanceAmount = 100.0;

        UpdateBalancePayload payload = new UpdateBalancePayload();
        payload.setCreditCardNumber(creditCardNumber);
        payload.setBalanceDate(balanceDate);
        payload.setBalanceAmount(balanceAmount);

        CreditCard creditCard = new CreditCard();

        // Mock creditCardRepository to return the credit card when findByNumber is
        // called
        when(creditCardRepository.findByNumber(creditCardNumber)).thenReturn(Optional.of(creditCard));
        // Mock balanceHistoryRepository to throw an exception
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenThrow(new RuntimeException());

        // Invoke the controller method
        ResponseEntity<?> response = creditCardController.updateBalance(new UpdateBalancePayload[] { payload });

        // Verify that the response status code is 400 BAD REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code is not BAD REQUEST");
    }
}
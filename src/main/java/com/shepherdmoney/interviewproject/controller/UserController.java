package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        try {
            // Create a new User entity with information given in the payload
            User newUser = new User();
            newUser.setName(payload.getName());
            newUser.setEmail(payload.getEmail());

            // Save the user in the database
            User savedUser = userRepository.save(newUser);

            // Return the id of the saved user in 200 OK response
            return ResponseEntity.ok(savedUser.getId());

        } catch (Exception e) {
            // If an exception occurs during user creation
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        try {
            // Check if a user with the given ID exists
            if (userRepository.existsById(userId)) {
                // Delete the user
                userRepository.deleteById(userId);
                // Return 200 OK if the deletion is successful
                return ResponseEntity.ok("User with ID " + userId + " deleted successfully.");
            } else {
                // Return 400 Bad Request if a user with the ID does not exist
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User with ID " + userId + " does not exist.");
            }
        } catch (Exception e) {
            // If an exception occurs during user creation
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Couldn't delete user with ID " + userId);
        }

    }
}

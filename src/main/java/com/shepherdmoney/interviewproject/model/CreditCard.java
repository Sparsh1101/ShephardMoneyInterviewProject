package com.shepherdmoney.interviewproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    // Storing credit card's owner here as foreign key, we can directly query a
    // credit card's owner using card number as required
    @ManyToOne
    private User owner;

    // Using Lob annotation for large objects
    // We can't store TreeMap in database so converting to JSON String to store
    // entire balance history of credit card
    @Lob
    private String balanceHistoryJson = null;

    // Using the Tree Map data structure for storing the balance History.
    // This will be a Transient field and won't be stored in database.
    // This is the best data structure given requirements.
    // Stores chronologically (Height Balanced binary search tree form)
    /*
     * We can directly query current date's balance using getCurrentBalance function
     * below (no need to store and efficient retrieval)
     */
    /*
     * Retrieval of a balance of a single day is O(1) if the date entry exists in
     * the map
     */
    /*
     * Retrieval of a balance of a single day is O(log n) if the date entry does not
     * exist in the map
     */
    // Traversal of the entire balance history is O(n) - cannot be faster
    // Insertion of a new balance is O(log n) due to automatic height balancing
    // Deletion of a balance is O(log n) due to automatic height balancing
    // Easily handles gaps - no need to store gaps
    /*
     * In the condition that there are gaps, retrieval of "closest" previous balance
     * date in O (log n) - using getBalanceOnDate function
     */
    @Transient
    private TreeMap<String, Double> balanceHistory;

    // Add a new balance to the history
    public void addBalanceHistory(BalanceHistory balance) {
        /*
         * If balance tree is not null, means that this credit card already has previous
         * balance history records
         */
        if (balanceHistoryJson != null && !balanceHistoryJson.isEmpty()) {
            // Deserialize the JSON string to a TreeMap
            deserializeBalanceHistoryHelper();
        } else {
            // If the JSON string is empty or null, create a new TreeMap
            balanceHistory = new TreeMap<>();
        }

        // Get update date
        LocalDate updateDate = balance.getDate();
        // Get old balance if it exists in the balance history tree
        double oldBalanceOnDate = getBalanceOnDate(updateDate);
        // Get new balance on date
        double newBalance = balance.getBalance();

        // Calculate the difference between the new balance and the old balance
        double balanceDifference = newBalance - oldBalanceOnDate;

        // Update balances for entries higher than the current date with the difference
        updateHigherEntriesHelper(updateDate, balanceDifference);

        // Add/Update Balance for updateDate
        balanceHistory.put(updateDate.toString(), newBalance);

        // Update the JSON string (serialize) after adding a new balance
        balanceHistoryJson = new Gson().toJson(balanceHistory);
    }

    // Remove a balance from the history
    /*
     * Even though this function is never used, I have made this function to show
     * fast delete implementation of removing a balance history
     */
    public void removeBalanceHistory(LocalDate date) {
        if (balanceHistoryJson != null && !balanceHistoryJson.isEmpty()) {
            // Deserialize the JSON string to a TreeMap
            deserializeBalanceHistoryHelper();

            // Get date to be removed
            String removeDateStr = date.toString();

            // Get the balance to be removed
            Double balanceToRemove = balanceHistory.get(removeDateStr);

            if (balanceToRemove != null) {
                // Remove the balance from the TreeMap
                balanceHistory.remove(removeDateStr);

                // Update balances for entries higher than the removed date
                updateHigherEntriesHelper(date, balanceToRemove);

                // Update the JSON string (serialize) after removing the balance
                balanceHistoryJson = new Gson().toJson(balanceHistory);
            } else {
                System.out.println("Date not in Balance History");
            }
        } else {
            System.out.println("Balance History is Empty");
        }
    }

    // Get the current date balance
    public double getCurrentBalance() {
        if (balanceHistoryJson != null && !balanceHistoryJson.isEmpty()) {
            // Deserialize the JSON string to a TreeMap
            deserializeBalanceHistoryHelper();
            /*
             * Returning the last entry as if the last date is stored or not, last entry
             * will always have balance of current date
             */
            return balanceHistory.lastEntry().getValue();
        }
        return 0.0; // Return 0 if no balance history exists
    }

    // Get the balance on a specific date
    public double getBalanceOnDate(LocalDate date) {
        if (balanceHistoryJson != null && !balanceHistoryJson.isEmpty()) {
            // Deserialize the JSON string to a TreeMap
            deserializeBalanceHistoryHelper();

            // Get balance date
            String balanceDateStr = date.toString();
            // Get balance on date
            Double balance = balanceHistory.get(balanceDateStr);

            if (balance != null) {
                // If date record exists, return balance
                return balance;
            } else {
                // If date record doesn't exist, return closest previous record
                Map.Entry<String, Double> previousEntry = balanceHistory.lowerEntry(balanceDateStr);
                return previousEntry.getValue();
            }
        } else {
            return 0.0;
        }
    }

    // Deserialize the JSON string to a TreeMap
    private void deserializeBalanceHistoryHelper() {
        if (balanceHistoryJson != null && !balanceHistoryJson.isEmpty()) {
            Type type = new TypeToken<TreeMap<String, Double>>() {
            }.getType();
            balanceHistory = new Gson().fromJson(balanceHistoryJson, type);
        }
    }

    // Update balances for entries higher than the updateAfterDate date with the
    // difference
    private void updateHigherEntriesHelper(LocalDate updateAfterDate, double balanceDifference) {
        NavigableMap<String, Double> higherEntries = balanceHistory.tailMap(updateAfterDate.toString(), false);
        for (Map.Entry<String, Double> entry : higherEntries.entrySet()) {
            entry.setValue(entry.getValue() + balanceDifference);
        }
    }
}

package com.reloop.model;

/**
 * Customer digital wallet holding accumulated reward points.
 * Ensures balance non-negativity and models credit/debit operations.
 */
public class Wallet {
    private int id;
    private int customerId;
    private int balance;

    public Wallet() {
        this.balance = 0;
    }

    public Wallet(int id, int customerId, int balance) {
        this.id = id;
        this.customerId = customerId;
        this.balance = balance;
    }

    /**
     * Credits points to the wallet.
     */
    public void credit(int points) {
        if (points > 0) {
            this.balance += points;
        }
    }

    /**
     * Debits points from the wallet, ensuring balance does not drop below zero.
     */
    public boolean debit(int points) {
        if (points > 0 && this.balance >= points) {
            this.balance -= points;
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}

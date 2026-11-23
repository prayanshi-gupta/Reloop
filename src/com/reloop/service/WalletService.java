package com.reloop.service;

import com.reloop.dao.TransactionDAO;
import com.reloop.dao.WalletDAO;
import com.reloop.exception.DatabaseException;
import com.reloop.model.PointsTransaction;
import com.reloop.model.Wallet;

import java.util.List;

/**
 * Service managing customer wallet balance inquiries and transactions.
 */
public class WalletService {

    private final WalletDAO walletDAO = new WalletDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Wallet getWalletByCustomerId(int customerId) throws DatabaseException {
        Wallet wallet = walletDAO.findByCustomerId(customerId);
        if (wallet == null) {
            // Return virtual wallet with 0 balance if not yet initialized
            return new Wallet(0, customerId, 0);
        }
        return wallet;
    }

    public List<PointsTransaction> getTransactions(int customerId) throws DatabaseException {
        Wallet wallet = walletDAO.findByCustomerId(customerId);
        if (wallet == null) {
            return List.of();
        }
        return transactionDAO.findByWalletId(wallet.getId());
    }
}

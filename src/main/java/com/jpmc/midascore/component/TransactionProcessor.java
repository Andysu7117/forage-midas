package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionProcessor(UserRepository userRepository, 
                               TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public void processTransaction(Transaction transaction) {
        // Validate sender exists
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        if (sender == null) {
            logger.debug("Transaction rejected: senderId {} not found", transaction.getSenderId());
            return;
        }

        // Validate recipient exists
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if (recipient == null) {
            logger.debug("Transaction rejected: recipientId {} not found", transaction.getRecipientId());
            return;
        }

        // Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.debug("Transaction rejected: sender {} has insufficient balance. Required: {}, Available: {}", 
                        sender.getId(), transaction.getAmount(), sender.getBalance());
            return;
        }

        // All validations passed - process the transaction
        // Update sender balance
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        userRepository.save(sender);

        // Update recipient balance
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());
        userRepository.save(recipient);

        // Create and save transaction record
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
        transactionRecordRepository.save(transactionRecord);

        logger.info("Transaction processed successfully: senderId={}, recipientId={}, amount={}", 
                   transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
    }
}


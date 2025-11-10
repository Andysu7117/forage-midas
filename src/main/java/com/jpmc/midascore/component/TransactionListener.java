package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    @Value("${general.kafka-topic}")
    private String topic;

    private final TransactionProcessor transactionProcessor;

    public TransactionListener(TransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void receiveTransaction(Transaction transaction) {
        logger.info("Received transaction: senderId={}, recipientId={}, amount={}", 
                   transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
        transactionProcessor.processTransaction(transaction);
    }
}


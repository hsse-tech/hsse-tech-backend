package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletUpdatingException;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService implements TransactionServiceBase {

  private final JpaWalletRepository jpaWalletRepository;
  private final JpaTransactionRepository jpaTransactionRepository;
  private final WalletService walletService;

  public TransactionService(JpaWalletRepository jpaWalletRepository,
                            JpaTransactionRepository jpaTransactionRepository,
                            WalletService walletService) {
    this.jpaWalletRepository = jpaWalletRepository;
    this.jpaTransactionRepository = jpaTransactionRepository;
    this.walletService = walletService;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Transaction createTransaction(TransactionInfo transactionInfo) {
    var wallet = jpaWalletRepository.findById(transactionInfo.walletId())
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found to assign a new transaction"));

    if (wallet.getBalance().compareTo(transactionInfo.amount()) < 0) {
      throw new WalletUpdatingException("Not enough money");
    }

    var transaction = new Transaction(transactionInfo.amount(), transactionInfo.name(), transactionInfo.description().orElse(null));

    transaction.setWallet(wallet);

    jpaTransactionRepository.save(transaction);

    return transaction;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Transaction setTransactionStatus(UUID id, ClientTransactionStatus status) {
    var targetTransaction = jpaTransactionRepository.findById(id)
            .orElseThrow(() ->
                    new TransactionNotFoundException("Transaction for status update not found"));

    targetTransaction.setStatus(status);
    jpaTransactionRepository.save(targetTransaction);

    return targetTransaction;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void commitTransaction(UUID id) {
    var target = jpaTransactionRepository.findById(id).orElseThrow(WalletNotFoundException::new);
    var targetWallet = target.getWallet();
    walletService.changeWalletBalanceOn(targetWallet.getId(), target.getAmount().negate());
    target.setStatus(ClientTransactionStatus.SUCCESS);

    jpaTransactionRepository.save(target);
  }

  @Override
  public void failTransaction(UUID id) {

  }
}

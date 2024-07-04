package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
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
            .orElseThrow(() -> EntityNotFoundException.walletNotFound(transactionInfo.walletId()));

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
  public void commitTransaction(UUID id) throws TransactionManipulationException {
    var target = jpaTransactionRepository.findById(id)
            .orElseThrow(() -> EntityNotFoundException.transactionNotFound(id));

    if (target.getStatus() != ClientTransactionStatus.IN_PROCESS) {
      throw new TransactionManipulationException();
    }

    var targetWallet = target.getWallet();
    walletService.changeWalletBalanceOn(targetWallet.getId(), target.getAmount().negate());
    target.setStatus(ClientTransactionStatus.SUCCESS);

    jpaTransactionRepository.save(target);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void failTransaction(UUID id) throws TransactionManipulationException {
    var targetTransaction = jpaTransactionRepository.findById(id)
            .orElseThrow(() -> EntityNotFoundException.transactionNotFound(id));

    if (targetTransaction.getStatus() != ClientTransactionStatus.IN_PROCESS) {
      throw new TransactionManipulationException();
    }

    targetTransaction.setStatus(ClientTransactionStatus.FAILED);

    jpaTransactionRepository.save(targetTransaction);
  }
}

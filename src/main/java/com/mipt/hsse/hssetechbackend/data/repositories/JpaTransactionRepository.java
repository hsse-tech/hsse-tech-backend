package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaTransactionRepository extends JpaRepository<Transaction, UUID> {}

package com.budgetmate.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetmate.receipt.entity.ReceiptEntity;

public interface ReceiptRepository extends JpaRepository <ReceiptEntity, Long>{

}

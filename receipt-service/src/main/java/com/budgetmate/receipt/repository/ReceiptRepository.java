package com.budgetmate.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgetmate.receipt.entity.ReceiptEntity;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<ReceiptEntity, Long> {
    List<ReceiptEntity> findByUserIdAndIsDeletedFalse(Long userId);
}

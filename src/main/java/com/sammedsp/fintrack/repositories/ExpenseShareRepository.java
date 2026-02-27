package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, String> {
}

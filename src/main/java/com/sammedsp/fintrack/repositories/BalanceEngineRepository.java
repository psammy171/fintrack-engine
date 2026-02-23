package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.BalanceEngine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceEngineRepository extends JpaRepository<BalanceEngine, String> {

    List<BalanceEngine> findByFolderId(String folderId);
}

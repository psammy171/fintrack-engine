package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.UserSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSettlementRepository extends JpaRepository<UserSettlement, String> {

    List<UserSettlement> findByFolderId(String folderId);
}

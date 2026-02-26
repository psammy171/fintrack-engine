package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.UserSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserSettlementRepository extends JpaRepository<UserSettlement, String> {

    List<UserSettlement> findByFolderId(String folderId);

    Optional<UserSettlement> findByFolderIdAndCreditorIdAndDebitorId(String folderId, String creditorId, String debitorId);
}

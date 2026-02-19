package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.SharedFolderUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedFolderUserRepository extends JpaRepository<SharedFolderUser, String> {
    List<SharedFolderUser> findAllByFolderId(String folderId);

    List<SharedFolderUser> findAllByUserId(String userId);
}

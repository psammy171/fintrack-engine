package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoldersRepository extends JpaRepository<Folder, String> {

    List<Folder> findAllByUserId(String userId);

    Optional<Folder> findByIdAndUserId(String id, String userId);

    List<Folder> findAllByIdIn(List<String> folderIds);
}

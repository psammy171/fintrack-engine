package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {

    public List<Tag> findByUserId(String userId);

    public List<Tag> findByUserIdAndFolderIdIsNull(String userId);

    public List<Tag> findByFolderId(String folderId);

    public Optional<Tag> findByIdAndUserId(String id, String userId);
}

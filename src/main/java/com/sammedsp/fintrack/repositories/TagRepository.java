package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {
    @Query(value = """
            SELECT
                t
            FROM
                tags t
            WHERE
                t.userId = :userId
            AND (
                :search IS NULL 
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )      
    """)
    List<Tag> findByUserId(@Param("userId") String userId, @Param("search") String search);

    @Query(value = """
            SELECT
                t
            FROM
                tags t
            WHERE
                t.userId = :userId
            AND
                t.folderId IS NULL
            AND (
                :search IS NULL 
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )      
    """)
    List<Tag> findByUserIdAndFolderIdIsNull(@Param("userId") String userId, @Param("search") String search);

    @Query(value = """
            SELECT
                t
            FROM
                tags t
            WHERE
                t.folderId = :folderId 
            AND (
                :search IS NULL
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
    """)
    public List<Tag> findByFolderId(@Param("folderId") String folderId, @Param("search") String search);

    public Optional<Tag> findByIdAndFolderId(String id, String folderId);

    public Optional<Tag> findByIdAndUserId(String id, String userId);

    public Integer countByUserId(String userId);
}

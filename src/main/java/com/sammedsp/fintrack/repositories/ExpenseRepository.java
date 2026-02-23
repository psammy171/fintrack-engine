package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.dtos.CurrentMonthExpenseSummary;
import com.sammedsp.fintrack.dtos.DailyExpensesByMonthSummary;
import com.sammedsp.fintrack.entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, String> {
    public Page<Expense> findAllByUserId(String userId, Pageable pageable);

    public Page<Expense> findAllByUserIdAndFolderIdIsNull(String userId, Pageable pageable);

    public Page<Expense> findAllByFolderId(String folderId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE expenses SET folder_id = NULL where user_id = :userId AND folder_id = :folderId", nativeQuery = true)
    public void moveExpensesToRootFolder(@Param("folderId") String folderId, @Param("userId") String userId);

    @Query(value = """
        SELECT
            SUM(e.amount) AS total,
            MONTHNAME(e.time) AS month
        FROM
            expenses e
        WHERE
            e.user_id =:userId
            AND MONTH(e.time) = MONTH(CURRENT_DATE())
            AND YEAR(e.time) = YEAR(CURRENT_DATE())
        GROUP BY month
    """, nativeQuery = true)
    public CurrentMonthExpenseSummary getCurrentMonthExpenseSummary(@Param("userId") String userId);

    @Query(value = """
        SELECT
            SUM(e.amount) AS total,
            DAY(e.time) AS day
        FROM
            expenses e
        WHERE
            e.user_id = :userId
            AND MONTH(e.time) = :month
            AND YEAR(e.time)  = :year
        GROUP BY day
        ORDER BY day ASC
    """, nativeQuery = true)
    public List<DailyExpensesByMonthSummary> getDailyExpensesByMonthSummary(
            @Param("userId") String userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}

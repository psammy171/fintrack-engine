package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.dtos.DailyExpensesByMonthSummary;
import com.sammedsp.fintrack.dtos.ExpenseSummaryQueryResult;
import com.sammedsp.fintrack.dtos.TopExpenseQueryResult;
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
            SUM(e.amount) AS total
        FROM
            expenses e
        WHERE
            e.user_id = :userId
            AND (:startDate IS NULL OR e.time >= :startDate)
            AND (:endDate IS NULL OR e.time <= :endDate)
            AND (:folderId IS NULL OR e.folder_id = :folderId)     
    """, nativeQuery = true)
    public ExpenseSummaryQueryResult fetchExpenseSummary(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("folderId") String folderId);


    @Query(value = """
        SELECT
            DATE_FORMAT(daily_expenses.time, '%Y-%m-%d') AS time,
            CAST(ROUND(daily_expenses.amount, 2) AS DOUBLE) AS amount
        FROM (
            SELECT
                SUM(e.amount) AS amount,
                e.time AS time
            FROM
                expenses e
            WHERE
                e.user_id = :userId
                AND (:startDate IS NULL OR e.time >= :startDate)
                AND (:endDate IS NULL OR e.time <= :endDate)
                AND (
                    (:folderId = "ROOT" AND e.folder_id IS NULL)
                    OR  (e.folder_id = :folderId)
                )
            GROUP BY e.time
        ) AS daily_expenses
        ORDER BY daily_expenses.amount DESC
        LIMIT 1
    """, nativeQuery = true)
    TopExpenseQueryResult fetchHighestExpense(
        @Param("userId") String userId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("folderId") String folderId
    );

    @Query(value = """
        SELECT
            DATE_FORMAT(daily_expenses.time, '%Y-%m-%d') AS time,
            CAST(ROUND(daily_expenses.amount, 2) AS DOUBLE) AS amount
        FROM (
            SELECT
                SUM(e.amount) AS amount,
                e.time AS time
            FROM
                expenses e
            WHERE
                e.user_id = :userId
                AND (:startDate IS NULL OR e.time >= :startDate)
                AND (:endDate IS NULL OR e.time <= :endDate)
                AND (
                    (:folderId = "ROOT" AND e.folder_id IS NULL)
                    OR  (e.folder_id = :folderId)
                )
            GROUP BY e.time
        ) AS daily_expenses
        ORDER BY daily_expenses.amount ASC
        LIMIT 1
    """, nativeQuery = true)
    TopExpenseQueryResult fetchLowestExpense(
        @Param("userId") String userId,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("folderId") String folderId
    );

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

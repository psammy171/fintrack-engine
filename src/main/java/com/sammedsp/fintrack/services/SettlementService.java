package com.sammedsp.fintrack.services;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sammedsp.fintrack.dtos.UserShareDto;
import com.sammedsp.fintrack.entities.UserSettlement;
import com.sammedsp.fintrack.repositories.UserSettlementRepository;

@Service
public class SettlementService {

    // Tolerance for float rounding noise; anything smaller is treated as "settled".
    private static final float EPSILON = 0.01f;

    private final UserSettlementRepository userSettlementRepository;

    SettlementService(UserSettlementRepository userSettlementRepository) {
        this.userSettlementRepository = userSettlementRepository;
    }

    @Transactional
    public void updateSettlements(String folderId, String paidBy, List<UserShareDto> userShares) {
        var existingSettlements = this.userSettlementRepository.findByFolderId(folderId);

        // Step 1: fold existing pairwise settlements into net balances per user.
        var netBalances = computeNetBalances(existingSettlements);

        // Step 2: apply the new expense on top of the net balances.
        for (UserShareDto userShare : userShares) {
            if (userShare.getUserId().equals(paidBy)) continue;

            float amount = userShare.getAmount();
            netBalances.merge(paidBy, amount, Float::sum);
            netBalances.merge(userShare.getUserId(), -amount, Float::sum);
        }

        // Step 3: recompute the minimal set of transactions that realize those net balances.
        var simplifiedSettlements = simplifyBalances(folderId, netBalances);

        // Step 4: replace the old ledger for this folder with the simplified one.
        this.userSettlementRepository.deleteAll(existingSettlements);
        this.userSettlementRepository.saveAll(simplifiedSettlements);
    }

    private Map<String, Float> computeNetBalances(List<UserSettlement> existingSettlements) {
        Map<String, Float> netBalances = new HashMap<>();
        for (UserSettlement settlement : existingSettlements) {
            // debitor owes creditor `amount` -> creditor's net position goes up, debitor's goes down.
            netBalances.merge(settlement.getCreditorId(), settlement.getAmount(), Float::sum);
            netBalances.merge(settlement.getDebitorId(), -settlement.getAmount(), Float::sum);
        }
        return netBalances;
    }

    private List<UserSettlement> simplifyBalances(String folderId, Map<String, Float> netBalances) {
        // Max-heap: user who is owed the most money first.
        PriorityQueue<Map.Entry<String, Float>> creditors =
                new PriorityQueue<>(Comparator.comparing((Map.Entry<String, Float> e) -> e.getValue()).reversed());
        // Min-heap: user who owes the most money first (most negative value first).
        PriorityQueue<Map.Entry<String, Float>> debtors =
                new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));

        for (var entry : netBalances.entrySet()) {
            if (entry.getValue() > EPSILON) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() < -EPSILON) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        List<UserSettlement> result = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            var creditor = creditors.poll();
            var debtor = debtors.poll();

            float creditAmount = creditor.getValue();
            float debtAmount = -debtor.getValue();
            float settledAmount = Math.min(creditAmount, debtAmount);

            result.add(new UserSettlement(folderId, creditor.getKey(), debtor.getKey(), settledAmount));

            float remainingCredit = creditAmount - settledAmount;
            float remainingDebt = debtAmount - settledAmount;

            if (remainingCredit > EPSILON) {
                creditors.add(new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCredit));
            }
            if (remainingDebt > EPSILON) {
                debtors.add(new AbstractMap.SimpleEntry<>(debtor.getKey(), -remainingDebt));
            }
        }

        return result;
    }
}
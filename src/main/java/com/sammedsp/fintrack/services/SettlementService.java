package com.sammedsp.fintrack.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sammedsp.fintrack.dtos.UserShareDto;
import com.sammedsp.fintrack.entities.UserSettlement;
import com.sammedsp.fintrack.repositories.UserSettlementRepository;

@Service
public class SettlementService {

    private final UserSettlementRepository userSettlementRepository;

    SettlementService(UserSettlementRepository userSettlementRepository){
        this.userSettlementRepository = userSettlementRepository;
    }

    public void updateSettlements(String folderId, String paidBy, List<UserShareDto> userShares) {
        var userSettlements = this.userSettlementRepository.findByFolderId(folderId);

        var newUserSettlements = new ArrayList<>(userSettlements);
        for(UserShareDto userShare: userShares) {
            if(userShare.getUserId().equals(paidBy)) continue;

            var creditorSettlement = this.findBalanceEngineOrDefault(newUserSettlements, paidBy, userShare.getUserId(), folderId);
            creditorSettlement.setAmount(creditorSettlement.getAmount() + userShare.getAmount());

            var debitorSettlement = this.findBalanceEngineOrDefault(newUserSettlements,userShare.getUserId(), paidBy, folderId);
            debitorSettlement.setAmount(debitorSettlement.getAmount() - userShare.getAmount());
        }

        this.userSettlementRepository.saveAll(newUserSettlements);
    }

     private UserSettlement findBalanceEngineOrDefault(List<UserSettlement> userSettlements, String paidBy, String paidFor, String folderId) {
        var userSettlement = userSettlements.stream().filter(settlement -> settlement.getCreditorId().equals(paidBy) && settlement.getDebitorId().equals(paidFor)).findAny();

        if(userSettlement.isEmpty()){
            var balanceEngine = new UserSettlement(folderId, paidBy, paidFor, 0F);
            userSettlements.add(balanceEngine);

            return balanceEngine;
        }

        return userSettlement.get();

    }
}

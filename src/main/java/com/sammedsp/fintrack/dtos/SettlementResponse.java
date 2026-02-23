package com.sammedsp.fintrack.dtos;

public record SettlementResponse ( PublicUser creditor, PublicUser debitor, Float amount) {
}

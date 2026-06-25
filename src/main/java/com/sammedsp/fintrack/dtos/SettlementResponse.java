package com.sammedsp.fintrack.dtos;

public record SettlementResponse ( SharedFolderUserResponse creditor, SharedFolderUserResponse debitor, Float amount) {
}

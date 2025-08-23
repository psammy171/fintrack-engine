package com.sammedsp.fintrack.dtos;

import com.sammedsp.fintrack.enums.UserRole;

public record Role(String id, UserRole role, String userId) {
}

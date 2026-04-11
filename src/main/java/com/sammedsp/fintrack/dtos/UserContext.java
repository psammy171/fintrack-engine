package com.sammedsp.fintrack.dtos;

import java.util.List;

import com.sammedsp.fintrack.enums.UserRole;

public record UserContext(String userId, String firstName, String lastName, String email, String userName, String displayUserName, String avatar, Boolean emailVerified, List<UserRole> roles) {
}
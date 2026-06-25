package com.sammedsp.fintrack.dtos;

public record SearchUserResponse(
    String userId,
    String firstName,
    String lastName,
    String userName
) {}

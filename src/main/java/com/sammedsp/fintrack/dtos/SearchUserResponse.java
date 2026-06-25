package com.sammedsp.fintrack.dtos;

public record SearchUserResponse(
    String id,
    String firstName,
    String lastName,
    String userName
) {}

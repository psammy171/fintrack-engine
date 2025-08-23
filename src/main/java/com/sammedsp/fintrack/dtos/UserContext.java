package com.sammedsp.fintrack.dtos;


public record UserContext(String userId, String firstName, String lastName, String email, Role[] roles) {
}

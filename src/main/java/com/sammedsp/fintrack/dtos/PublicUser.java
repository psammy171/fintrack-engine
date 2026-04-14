package com.sammedsp.fintrack.dtos;

import com.sammedsp.fintrack.enums.UserRole;

public record PublicUser(
     String id,
     String firstName,
     String lastName,
     String email,
     String userName,
     String displayUserName,
     String avatar,
     Boolean emailVerified,
     UserRole[] roles){
}

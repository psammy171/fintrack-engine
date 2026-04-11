package com.sammedsp.fintrack.dtos;

public record PublicUser(String id,
     String firstName,
     String lastName,
     String email,
     String userName,
     String displayUserName,
     String avatar,
     Boolean emailVerified){
}

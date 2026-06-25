package com.sammedsp.fintrack.dtos;

public record SharedFolderUserResponse(
    String userId,
    String firstName,
    String lastName,
    String userName,
    String folderId,
    String ownerId
) {}

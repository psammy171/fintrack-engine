package com.sammedsp.fintrack.dtos;

public record SharedFolderUserResponse(
    String id,
    String firstName,
    String lastName,
    String userName,
    String folderId,
    String ownerId
) {}

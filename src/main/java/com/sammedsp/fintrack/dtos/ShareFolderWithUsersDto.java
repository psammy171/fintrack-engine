package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.Size;

public class ShareFolderWithUsersDto {

    @Size(min = 1, max = 10, message = "Minimum one user is required")
    String[] userIds;

    public String[] getUserIds(){
        return userIds;
    }
}

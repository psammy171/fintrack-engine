package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateFolderDto {

    @NotBlank
    @Size(min = 3, max = 50, message = "Folder name should be minimum 3 characters and max 50 characters")
    private String name;

    public String getName() {
        return name;
    }
}

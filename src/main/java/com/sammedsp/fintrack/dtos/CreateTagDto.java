package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CreateTagDto {

    @NotEmpty
    @Size(min = 3, message = "Tag should be minimum 3 characters")
    String name;

    public String getName() {
        return name;
    }
}

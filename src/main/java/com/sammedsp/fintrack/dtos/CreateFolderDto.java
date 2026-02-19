package com.sammedsp.fintrack.dtos;

public class CreateFolderDto extends UpdateFolderDto  {
    private boolean shared;

    public boolean isShared(){ return shared;}
}

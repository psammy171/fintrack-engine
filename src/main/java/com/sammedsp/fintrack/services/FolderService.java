package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CreateFolderDto;
import com.sammedsp.fintrack.entities.Folder;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.FoldersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FolderService {

    private final FoldersRepository foldersRepository;

    FolderService(FoldersRepository foldersRepository){
        this.foldersRepository = foldersRepository;
    }

    public List<Folder> getAllUsersFolders(String userId){
        return this.foldersRepository.findAllByUserId(userId);
    }

    public Folder createFolder(String userId, CreateFolderDto createFolderDto){
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(createFolderDto.getName());

        return this.foldersRepository.save(folder);
    }

    public Folder updateFolderName(String userId, String folderId, CreateFolderDto createFolderDto) throws EntityNotFoundException {
        Folder folder = this.findByIdAndUserIdOrThrow(folderId, userId);

        folder.setName(createFolderDto.getName());

        return this.foldersRepository.save(folder);
    }

    private Folder findByIdAndUserIdOrThrow(String id, String userId) throws EntityNotFoundException {
        Optional<Folder> folder = this.foldersRepository.findByIdAndUserId(id, userId);

        if(folder.isEmpty()){
            throw new EntityNotFoundException(Folder.class.getName(),id);
        }

        return folder.get();
    }
}

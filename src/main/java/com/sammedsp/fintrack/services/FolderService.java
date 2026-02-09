package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CreateFolderDto;
import com.sammedsp.fintrack.entities.Folder;
import com.sammedsp.fintrack.exceptions.BadRequestException;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.repositories.FoldersRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FolderService {

    private final ExpenseRepository expenseRepository;
    private final FoldersRepository foldersRepository;

    FolderService(FoldersRepository foldersRepository, ExpenseRepository expenseRepository){
        this.foldersRepository = foldersRepository;
        this.expenseRepository = expenseRepository;
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

    public Folder shareFolder(String userId, String folderId) throws EntityNotFoundException {
        Folder folder = this.findByIdAndUserIdOrThrow(folderId, userId);
        folder.setShared(true);

        return this.foldersRepository.save(folder);
    }

    @Transactional
    public void deleteFolderAndMoveExpensesToRootFolder(String folderId, String userId) throws EntityNotFoundException {
        Folder folder = this.findByIdAndUserIdOrThrow(folderId, userId);

        if(folder.isShared()){
            throw new BadRequestException("Shared folder can not be deleted!");
        }

        this.expenseRepository.moveExpensesToRootFolder(folderId, userId);
        this.foldersRepository.delete(folder);
    }

    public Folder findByIdAndUserIdOrThrow(String id, String userId) throws EntityNotFoundException {
        Optional<Folder> folder = this.foldersRepository.findByIdAndUserId(id, userId);

        if(folder.isEmpty()){
            throw new EntityNotFoundException(Folder.class.getName(),id);
        }

        return folder.get();
    }
}

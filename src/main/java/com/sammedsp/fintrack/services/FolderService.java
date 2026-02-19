package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CreateFolderDto;
import com.sammedsp.fintrack.dtos.PublicUser;
import com.sammedsp.fintrack.dtos.ShareFolderWithUsersDto;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.entities.Folder;
import com.sammedsp.fintrack.entities.SharedFolderUser;
import com.sammedsp.fintrack.exceptions.BadRequestException;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.repositories.FoldersRepository;
import com.sammedsp.fintrack.repositories.SharedFolderUserRepository;
import com.sammedsp.fintrack.security.Oauth2Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final ExpenseRepository expenseRepository;
    private final FoldersRepository foldersRepository;
    private final Oauth2Service oauth2Service;
    private final SharedFolderUserRepository sharedFolderUserRepository;

    FolderService(FoldersRepository foldersRepository, ExpenseRepository expenseRepository, SharedFolderUserRepository sharedFolderUserRepository, Oauth2Service oauth2Service){
        this.foldersRepository = foldersRepository;
        this.expenseRepository = expenseRepository;
        this.sharedFolderUserRepository = sharedFolderUserRepository;
        this.oauth2Service = oauth2Service;
    }

    public List<Folder> getAllUsersFolders(String userId){
        var folders = this.foldersRepository.findAllByUserId(userId);
        var sharedFolders = this.getSharedFolders(userId);

        folders.addAll(sharedFolders);
        return folders;
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

    public List<PublicUser> shareFolderWithUsers(String userId, String folderId, ShareFolderWithUsersDto shareFolderWithUsersDto) {
        Folder folder = this.findByIdAndUserIdOrThrow(folderId, userId);

        if(!folder.isShared()){
            throw new BadRequestException("Folder with id " + folderId + " is not shared");
        }

        List<SharedFolderUser> existingSharedFolderUsers = this.sharedFolderUserRepository.findAllByFolderId(folderId);

        String[] newUserIds = this.getNewUserIds(existingSharedFolderUsers, shareFolderWithUsersDto.getUserIds());

        if(existingSharedFolderUsers.size() + newUserIds.length > 10){
            throw new BadRequestException("Folder can be shared with max 10 users");
        }

        List<PublicUser> userProfiles = this.validateUserIds(newUserIds);

        this.shareFolder(folderId, userProfiles);

        return userProfiles;
    }

    public List<PublicUser> fetchSharedFolderUsers(String folderId, UserContext user) {
        var userId = user.userId();

       var isFolderAccessible = this.checkIdFolderIsAccessible(folderId, userId);
       if(!isFolderAccessible){
           throw new BadRequestException("Folder with id " + folderId + " not found");
       }

       var folder = this.findByIdOrThrow(folderId);

       var sharedFolderUsers = this.sharedFolderUserRepository.findAllByFolderId(folderId);
       sharedFolderUsers.add(this.getSharedFolderUser(folderId, folder.getUserId()));

       var userIds = sharedFolderUsers.stream().map(SharedFolderUser::getUserId).toArray(String[]::new);


       return this.oauth2Service.getUserInfoByUserIds(userIds);
    }

    private boolean checkIdFolderIsAccessible(String folderId, String userId) {
        return isFolderOwner(folderId, userId) || isSharedFolderUser(folderId, userId);
    }

    private boolean isSharedFolderUser(String folderId, String userId) {
        var sharedFolderUser = this.sharedFolderUserRepository.findAllByFolderId(folderId);

        return sharedFolderUser.stream().anyMatch(user -> user.getUserId().equals(userId));
    }

    private boolean isFolderOwner(String folderId, String userId) {
        Optional<Folder> folder = this.foldersRepository.findByIdAndUserId(folderId, userId);

        return folder.isPresent();
    }

    private String[] getNewUserIds(List<SharedFolderUser> existingSharedFolderUsers, String[] userIds){
        Set<String> existingIds = existingSharedFolderUsers.stream()
                .map(SharedFolderUser::getUserId) // Assuming the getter name
                .collect(Collectors.toSet());

        return Arrays.stream(userIds).filter(userId -> !existingIds.contains(userId)).toArray(String[]::new);
    }

    private List<PublicUser> validateUserIds(String [] userIds) {
        if(userIds.length == 0) return List.of();

        List<PublicUser> users = this.oauth2Service.getUserInfoByUserIds(userIds);
        if(users.size() != userIds.length)
            throw new BadRequestException("Invalid user ids");

        return users;
    }

    private void shareFolder(String folderId, List<PublicUser> userProfiles) {
        List<SharedFolderUser> sharedFolderUsers = userProfiles.stream().map(user -> this.getSharedFolderUser(folderId, user.userId())).toList();

        this.sharedFolderUserRepository.saveAll(sharedFolderUsers);
    }

    private SharedFolderUser getSharedFolderUser(String folderId, String userId) {
        var sharedUser = new SharedFolderUser();

        sharedUser.setFolderId(folderId);
        sharedUser.setUserId(userId);

        return sharedUser;
    }

    private List<Folder> getSharedFolders(String userId) {
        var sharedFolders = this.sharedFolderUserRepository.findAllByUserId(userId);

        var folderIds = sharedFolders.stream().map(SharedFolderUser::getFolderId).toList();

        return this.foldersRepository.findAllByIdIn(folderIds);
    }

    private Folder findByIdOrThrow(String folderId) {
        var folder = this.foldersRepository.findById(folderId);

        if(folder.isPresent()){
            return folder.get();
        }

        throw new BadRequestException("Folder with id " + folderId + " not found");
    }
}

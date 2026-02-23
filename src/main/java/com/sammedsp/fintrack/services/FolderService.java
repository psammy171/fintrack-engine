package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.*;
import com.sammedsp.fintrack.entities.Folder;
import com.sammedsp.fintrack.entities.SharedFolderUser;
import com.sammedsp.fintrack.entities.UserSettlement;
import com.sammedsp.fintrack.exceptions.BadRequestException;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.repositories.FoldersRepository;
import com.sammedsp.fintrack.repositories.SharedFolderUserRepository;
import com.sammedsp.fintrack.repositories.UserSettlementRepository;
import com.sammedsp.fintrack.security.Oauth2Service;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final ExpenseRepository expenseRepository;
    private final FoldersRepository foldersRepository;
    private final Oauth2Service oauth2Service;
    private final UserSettlementRepository userSettlementRepository;
    private final SharedFolderUserRepository sharedFolderUserRepository;

    FolderService(FoldersRepository foldersRepository, ExpenseRepository expenseRepository, SharedFolderUserRepository sharedFolderUserRepository, Oauth2Service oauth2Service, UserSettlementRepository userSettlementRepository){
        this.foldersRepository = foldersRepository;
        this.expenseRepository = expenseRepository;
        this.sharedFolderUserRepository = sharedFolderUserRepository;
        this.oauth2Service = oauth2Service;
        this.userSettlementRepository = userSettlementRepository;
    }

    public FolderSettlements getSharedFolderSettlements(String folderId, String userId) {
        var folder = this.findFolderWithUserAccess(folderId, userId);

        if(!folder.isShared()){
            throw new BadRequestException("Folder with id " + folderId + " not found");
        }

        var sharedFolderUsers = this.fetchSharedFolderUsers(folderId, userId);
        var userSettlements = this.userSettlementRepository.findByFolderId(folderId);

        return this.mapUserSettlementsAndSharedUser(userSettlements, sharedFolderUsers);
    }

    public List<Folder> getAllUsersFolders(String userId, String scope){
        var folders = this.foldersRepository.findAllByUserId(userId);

        if("owned".equals(scope)) return folders;

        var sharedFolders = this.getSharedFolders(userId);

        folders.addAll(sharedFolders);
        return folders;
    }

    public Folder createFolder(String userId, CreateFolderDto createFolderDto){
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(createFolderDto.getName());
        folder.setShared(createFolderDto.isShared());

        return this.foldersRepository.save(folder);
    }

    public Folder updateFolderName(String userId, String folderId, UpdateFolderDto updateFolderDto) throws EntityNotFoundException {
        Folder folder = this.findByIdAndUserIdOrThrow(folderId, userId);

        folder.setName(updateFolderDto.getName());

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

    public List<PublicUser> fetchSharedFolderUsers(String folderId, String userId) {
       var isFolderAccessible = this.checkIfSharedFolderIsAccessible(folderId, userId);
       if(!isFolderAccessible){
           throw new BadRequestException("Folder with id " + folderId + " not found");
       }

       var folder = this.findByIdOrThrow(folderId);

       var sharedFolderUsers = this.sharedFolderUserRepository.findAllByFolderId(folderId);
       sharedFolderUsers.add(this.getSharedFolderUser(folderId, folder.getUserId()));

       var userIds = sharedFolderUsers.stream().map(SharedFolderUser::getUserId).toArray(String[]::new);


       return this.oauth2Service.getUserInfoByUserIds(userIds);
    }

    public List<String> fetchSharedFolderUserIds(String folderId){
        var folder = this.findByIdOrThrow(folderId);
        var sharedFolderUsers = this.sharedFolderUserRepository.findAllByFolderId(folderId);

        List<String> userIds = new ArrayList<>(sharedFolderUsers.stream().map(SharedFolderUser::getUserId).toList());
        userIds.add(folder.getUserId());

        return userIds;
    }

    public void checkSharedFolderAccessOrThrow(String folderId, String userId) {
        var isFolderAccessible = this.checkIfSharedFolderIsAccessible(folderId, userId);

        if(!isFolderAccessible)
            throw new BadRequestException("Folder with id " + folderId + " not found!");
    }

    public Folder findFolderWithUserAccess(String folderId, String userId) {
        var folder = this.findByIdOrThrow(folderId);

        if(isFolderOwner(folder, userId)) return folder;

        if(!folder.isShared()){
            throw new BadRequestException("Folder with id " + folderId + " not found");
        }

        var isSharedFolder = this.isSharedFolderUser(folderId, userId);

        if(!isSharedFolder) {
            throw new BadRequestException("Folder with id " + folderId + " not found");
        }

        return folder;
    }

    public Folder findByIdOrThrow(String folderId) {
        var folder = this.foldersRepository.findById(folderId);

        if(folder.isPresent()){
            return folder.get();
        }

        throw new BadRequestException("Folder with id " + folderId + " not found");
    }

    private boolean checkIfSharedFolderIsAccessible(String folderId, String userId) {
        var folder = this.findByIdOrThrow(folderId);

        if(!folder.isShared()) return false;

        return isFolderOwner(folder, userId) || isSharedFolderUser(folderId, userId);
    }

    public boolean isSharedFolderOwner(String folderId, String userId) {
        var folder = this.findByIdAndUserIdOrThrow(folderId, userId);

        return folder.isShared();
    }

    private boolean isSharedFolderUser(String folderId, String userId) {
        var sharedFolderUser = this.sharedFolderUserRepository.findAllByFolderId(folderId);

        return sharedFolderUser.stream().anyMatch(user -> user.getUserId().equals(userId));
    }

    private boolean isFolderOwner(Folder folder, String userId) {
        return folder.getUserId().equals(userId);
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

    private FolderSettlements mapUserSettlementsAndSharedUser(List<UserSettlement> userSettlements, List<PublicUser> sharedFolderUsers) {
        var positiveUserSettlements = userSettlements.stream().filter(settlement -> settlement.getAmount() > 0).toList();

        ArrayList<SettlementResponse> settlements  = new ArrayList<>();

        for(UserSettlement userSettlement: positiveUserSettlements) {
            var creditor = this.findUser(sharedFolderUsers, userSettlement.getCreditorId());
            var debitor = this.findUser(sharedFolderUsers, userSettlement.getDebitorId());
            var amount = userSettlement.getAmount();
            var settlement = new SettlementResponse(creditor, debitor, amount);

            settlements.add(settlement);
        }

        return new FolderSettlements(settlements);
    }

    private PublicUser findUser( List<PublicUser> sharedFolderUsers, String userId) {
        var user = sharedFolderUsers.stream().filter(sharedUser -> sharedUser.userId().equals(userId)).findAny();

        if(user.isEmpty()){
            throw new BadRequestException("Something went wrong while fetching settlements");
        }

        return user.get();

    }
}

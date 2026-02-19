package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.*;
import com.sammedsp.fintrack.entities.Folder;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.services.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FoldersController {

    private final FolderService folderService;

    FoldersController(FolderService folderService){
        this.folderService = folderService;
    }

    @GetMapping()
    public ResponseEntity<List<Folder>> getUsersFolders(Authentication authentication){
        UserContext userContext = (UserContext) authentication.getPrincipal();

        List<Folder> folders = this.folderService.getAllUsersFolders(userContext.userId());

        return ResponseEntity.ok(folders);
    }

    @PostMapping()
    public ResponseEntity<Folder> createFolder(Authentication authentication, @Valid @RequestBody CreateFolderDto createFolderDto){
        UserContext userContext = (UserContext) authentication.getPrincipal();

        Folder folder = this.folderService.createFolder(userContext.userId(), createFolderDto);

        return ResponseEntity.ok(folder);
    }

    @PatchMapping("/{folderId}")
    public ResponseEntity<Folder> updateFolder(Authentication authentication, @Valid @RequestBody CreateFolderDto createFolderDto, @PathVariable("folderId") String folderId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        Folder folder = this.folderService.updateFolderName(userContext.userId(), folderId, createFolderDto);

        return ResponseEntity.ok(folder);
    }

    @PatchMapping("/{folderId}/add-users")
    public ResponseEntity<ListResponse<PublicUser>> shareFolderWithUsers(Authentication authentication, @PathVariable("folderId") String folderId, @Valid @RequestBody ShareFolderWithUsersDto shareFolderWithUsersDto){
        UserContext userContext = (UserContext) authentication.getPrincipal();

        List<PublicUser> publicUsers = this.folderService.shareFolderWithUsers(userContext.userId(), folderId, shareFolderWithUsersDto);

        var listResponse = new ListResponse<>(publicUsers);
        return ResponseEntity.ok(listResponse);
    }

    @GetMapping("/{folderId}/shared-users")
    public ResponseEntity<ListResponse<PublicUser>> getSharedFolderUsers(Authentication authentication, @PathVariable("folderId") String folderId){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        var sharedFolderUsers = this.folderService.fetchSharedFolderUsers(folderId, userContext);

        return ResponseEntity.ok(new ListResponse<>(sharedFolderUsers));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<DeleteResponse> deleteFolder(Authentication authentication, @PathVariable("folderId") String folderId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        this.folderService.deleteFolderAndMoveExpensesToRootFolder(folderId, userContext.userId());

        DeleteResponse deleteResponse = new DeleteResponse(Folder.class.getName(), folderId, "Folder with id " + folderId + " deleted");

        return ResponseEntity.ok(deleteResponse);
    }
}

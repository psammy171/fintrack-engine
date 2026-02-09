package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.CreateFolderDto;
import com.sammedsp.fintrack.dtos.DeleteResponse;
import com.sammedsp.fintrack.dtos.UserContext;
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

    @PatchMapping("/{folderId}/share")
    public ResponseEntity<Folder> shareFolder(Authentication authentication, @PathVariable("folderId") String folderId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        Folder folder = this.folderService.shareFolder(userContext.userId(), folderId);

        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<DeleteResponse> deleteFolder(Authentication authentication, @PathVariable("folderId") String folderId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        this.folderService.deleteFolderAndMoveExpensesToRootFolder(folderId, userContext.userId());

        DeleteResponse deleteResponse = new DeleteResponse(Folder.class.getName(), folderId, "Folder with id " + folderId + " deleted");

        return ResponseEntity.ok(deleteResponse);
    }
}

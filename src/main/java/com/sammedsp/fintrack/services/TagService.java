package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CreateTagDto;
import com.sammedsp.fintrack.dtos.SetTagBudgetDto;
import com.sammedsp.fintrack.dtos.UpdateTagDto;
import com.sammedsp.fintrack.entities.Tag;
import com.sammedsp.fintrack.exceptions.BadRequestException;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final FolderService folderService;

    TagService(TagRepository tagRepository, FolderService folderService){
        this.tagRepository = tagRepository;
        this.folderService = folderService;
    }

    public Tag createTag(String userId, CreateTagDto createTagDto){
        Tag tag = this.getTagEntity(createTagDto, userId);
        return this.tagRepository.save(tag);
    }

    public List<Tag> getAllTags(String userId, String folderId){
        if(folderId == null)
            return this.tagRepository.findByUserIdAndFolderIdIsNull(userId);

        folderService.checkFolderAccessOrThrow(folderId, userId);
        return this.tagRepository.findByFolderId(folderId);
    }

    public Tag updateTag(String tagId, String userId, UpdateTagDto updateTagDto) throws EntityNotFoundException {
        Tag tag = this.findTagByIdAndUserIdOrThrow(tagId, userId);
        tag.setName(updateTagDto.getName());
        return this.tagRepository.save(tag);
    }

    public Tag setTagBudget(String tagId, String userId, SetTagBudgetDto setTagBudgetDto) throws EntityNotFoundException {
        Tag tag = this.findTagByIdAndUserIdOrThrow(tagId, userId);
        tag.setTagBudgetPeriod(setTagBudgetDto.getTagBudgetPeriod());
        tag.setBudget(setTagBudgetDto.getBudget());
        return this.tagRepository.save(tag);
    }

    public Tag findTagByIdAndUserIdOrThrow(String tagId, String userId) throws EntityNotFoundException {
        Optional<Tag> tag = this.tagRepository.findByIdAndUserId(tagId, userId);
        if(tag.isEmpty()){
            throw new EntityNotFoundException(Tag.class.getName(), tagId);
        }

        return tag.get();
    }

    private Tag getTagEntity(CreateTagDto createTagDto, String userId) {
        var folderId = createTagDto.getFolderId();

        if(folderId == null) {
            return new Tag(createTagDto.getName(), userId);
        }

        var isSharedAccessibleFolder = this.folderService.checkIfSharedFolderIsAccessible(folderId, userId);
        if(!isSharedAccessibleFolder) throw new BadRequestException("Folder with id " + folderId + " not found");

        return new Tag(createTagDto.getName(), userId, folderId);
    }
}

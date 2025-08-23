package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.SetTagBudgetDto;
import com.sammedsp.fintrack.entities.Tag;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    private final TagRepository tagRepository;

    TagService(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }

    public Tag createTag(String name, String userId){
        Tag tag = new Tag(name, userId);
        return this.tagRepository.save(tag);
    }

    public List<Tag> getAllUserTags(String userId){
        return this.tagRepository.findByUserId(userId);
    }

    public Tag updateTag(String tagId, String name, String userId) throws EntityNotFoundException {
        Tag tag = this.findTagByIdAndUserIdOrThrow(tagId, userId);
        tag.setName(name);
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
}

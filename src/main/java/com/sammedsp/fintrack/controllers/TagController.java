package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.SetTagBudgetDto;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.entities.Tag;
import com.sammedsp.fintrack.dtos.CreateTagDto;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.services.TagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    TagController(TagService tagService){
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllUserTags(Authentication authentication){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        List<Tag> tags = this.tagService.getAllUserTags(userId);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(Authentication authentication, @Valid @RequestBody CreateTagDto createTagDto){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        String name = createTagDto.getName();
        Tag tag = this.tagService.createTag(name, userId);
        return ResponseEntity.ok(tag);

    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<Tag> updateTag(Authentication authentication, @Valid @RequestBody CreateTagDto createTagDto, @PathVariable("tagId") String tagId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        Tag tag = this.tagService.updateTag(tagId, createTagDto.getName(), userId);
        return ResponseEntity.ok(tag);
    }

    @PatchMapping("/{tagId}/set-budget")
    public ResponseEntity<Tag> setBudgetForTag(Authentication authentication, @Valid @RequestBody SetTagBudgetDto setTagBudgetDto, @PathVariable("tagId") String tagId) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        Tag tag = this.tagService.setTagBudget(tagId, userId, setTagBudgetDto);

        return ResponseEntity.ok(tag);
    }
}

package com.fifteen.auction.domain.tag.controller;

import com.fifteen.auction.domain.tag.dto.request.TagCreateRequest;
import com.fifteen.auction.domain.tag.service.TagService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tags")
@RequiredArgsConstructor
public class TagAdminController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<Void> createTags(@RequestBody TagCreateRequest request) {
        tagService.createTagsOrThrowIfExists(request.getTagNames()); // 중복 검사 및 생성
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTag(@RequestParam String name) {
        tagService.deleteByName(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Response<List<String>>> findAllTags() {
        return ResponseEntity.ok(Response.of(tagService.findAllTagNames()));
    }
}

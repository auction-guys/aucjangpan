package com.fifteen.auction.domain.tag.controller;

import com.fifteen.auction.domain.tag.dto.response.TagResponse;
import com.fifteen.auction.domain.tag.service.TagService;
import com.fifteen.auction.global.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Response<List<TagResponse>>> getTags() {
        return ResponseEntity.ok(Response.of(tagService.findAll()));
    }
}
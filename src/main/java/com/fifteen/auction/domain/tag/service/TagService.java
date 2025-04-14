package com.fifteen.auction.domain.tag.service;

import com.fifteen.auction.domain.tag.entity.Tag;
import com.fifteen.auction.domain.tag.repository.TagRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public void createTagsOrThrowIfExists(List<String> tagNames) {
        List<String> trimmed = tagNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<Tag> existing = tagRepository.findAllByNameInIgnoreCase(trimmed);

        if (!existing.isEmpty()) {
            String exists = existing.stream().map(Tag::getName).collect(Collectors.joining(", "));
            throw new ClientException(ErrorCode.DUPLICATE_TAG);
        }

        List<Tag> newTags = trimmed.stream().map(Tag::create).toList();
        tagRepository.saveAll(newTags);
    }

    @Transactional
    public void deleteByName(String tagName) {
        String name = tagName.trim();
        Tag tag = tagRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ClientException(ErrorCode.DELETE_TAG_NOT_FOUND));
        tagRepository.delete(tag);
    }

    @Transactional(readOnly = true)
    public List<String> findAllTagNames() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Tag> findExistingTagsOnly(List<String> tagNames) {
        List<String> trimmed = tagNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<Tag> foundTags = tagRepository.findAllByNameInIgnoreCase(trimmed);

        if (foundTags.size() != trimmed.size()) {
            throw new ClientException(ErrorCode.TAG_NOT_FOUND);
        }
        return foundTags;
    }
}

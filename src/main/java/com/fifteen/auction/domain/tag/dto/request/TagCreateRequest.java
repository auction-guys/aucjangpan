package com.fifteen.auction.domain.tag.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TagCreateRequest {

    @NotEmpty
    private List<String> tagNames;

    public static TagCreateRequest of(List<String> tags) {
        return new TagCreateRequest(tags);
    }
}
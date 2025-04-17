package com.fifteen.auction.domain.tag.dto.response;

import com.fifteen.auction.domain.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class TagResponse {
    private final Long id;
    private final String name;

    public static TagResponse from(Tag tag) {
        return of(tag.getId(), tag.getName());
    }
}

package com.fifteen.auction.global.client.chatgpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;
}
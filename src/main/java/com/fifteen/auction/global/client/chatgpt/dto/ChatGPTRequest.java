package com.fifteen.auction.global.client.chatgpt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatGPTRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
}
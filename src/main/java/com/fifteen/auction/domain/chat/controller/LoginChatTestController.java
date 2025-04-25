package com.fifteen.auction.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginChatTestController {
    @GetMapping("/v1")
    public String showLoginPageV1() {
        return "loginV1";
    }

    @GetMapping("/v2")
    public String showLoginPageV2() {
        return "loginV2";
    }
}

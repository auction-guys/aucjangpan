package com.fifteen.auction.global.config.websocket;

import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                try{
                    String jwt = jwtUtil.substringToken(token);
                    Claims claims = jwtUtil.extractClaims(jwt);

                    Long userId = Long.valueOf(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String nickname = claims.get("nickname", String.class);

                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("email", email);
                    accessor.getSessionAttributes().put("nickname", nickname);

                    log.info("[WebSocket 인증 성공] userId: {}, email: {}", userId, email);
                } catch (Exception e){
                    log.error("WebSocket 인증 실패 {}", e.getMessage());
                    throw new MessagingException("JWT 인증 실패");
                }
            }
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            Object userId = accessor.getSessionAttributes().get("userId");

            if (userId == null) {
                log.warn("SEND: WebSocket세션에 사용자 정보 없음");
                throw new MessagingException("세션 인증 정보 없음");
            }

            log.info("SEND: userId={} ", userId);
        }
        return message;
    }
}

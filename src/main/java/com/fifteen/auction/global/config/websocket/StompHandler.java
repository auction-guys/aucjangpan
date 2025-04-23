package com.fifteen.auction.global.config.websocket;

import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            handleSend(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[WebSocket CONNECT] 헤더 없음 또는 형식 오류");
            throw new MessagingException("인증 헤더가 없거나 'Bearer ' 형식이 아닙니다.");
        }

        String jwt = authHeader.substring(7).trim();

        try {

            Claims claims = jwtUtil.extractClaims(jwt);

            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            String nickname = claims.get("nickname", String.class);

            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("email", email);
            accessor.getSessionAttributes().put("nickname", nickname);

            log.info("[WebSocket 인증 성공] userId: {}, email: {}", userId, email);
        } catch (JwtException e) {
            log.error("[WebSocket 인증 실패] 유효하지 않은 토큰: {}, JWT: {}", e.getMessage(), jwt);
        } catch (Exception e) {
            log.error("[WebSocket 인증 실패] 예상치 못한 오류 발생", e);
            throw new MessagingException("WebSocket 연결 중 오류 발생");
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        Object userId = accessor.getSessionAttributes().get("userId");

        if (userId == null) {
            log.warn("[WebSocket SEND] 세션에 사용자 정보 없음");
            throw new MessagingException("세션 인증 정보 없음");
        }
        log.info("[WebSocket SEND] 인증된 사용자 메시지 전송: userId={} ", userId);
    }

}

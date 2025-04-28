package com.fifteen.auction.global.config.websocket;

import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StompHandlerTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock // preSend 메서드의 파라미터 Mock 객체
    private MessageChannel messageChannel;

    @InjectMocks // @Mock으로 만든 객체들을 실제 StompHandler 객체에 주입
    private StompHandler stompHandler;

    private StompHeaderAccessor accessor;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
     void setUp() {
        // 각 테스트 전에 세션 속성 초기화
        sessionAttributes = new HashMap<>();
    }

    @Nested
    class ConnectTest{
        @Test
        void 사용자가_유효한_JWT로_WebSocket_연결_성공(){
            String validJwt = "valid.jwt.token";
            Long userId = 1L;
            String email = "test@example.com";
            String nickname = "tester";

            // Claim 객체 생성
            Claims mockClaims = Jwts.claims();
            mockClaims.setSubject(String.valueOf(userId));
            mockClaims.put("email", email);
            mockClaims.put("nickname", nickname);

            when(jwtUtil.extractClaims(validJwt)).thenReturn(mockClaims);

            // Stomp 헤더 설정
            accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("Authorization", "Bearer " + validJwt);
            accessor.setSessionAttributes(sessionAttributes); // 초기화된 세션 속성 설정
            Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

            assertDoesNotThrow(() -> {
                stompHandler.preSend(message, messageChannel);
            });

            // jwtUtil.extractClaims가 정확히 1번 호출되었는지 검증
            verify(jwtUtil, times(1)).extractClaims(validJwt);

            // 세션 속성이 올바르게 설정되었는지 검증
            Map<String, Object> actualAttributes = accessor.getSessionAttributes();
            assertNotNull(actualAttributes);
            assertEquals(userId, actualAttributes.get("userId"));
            assertEquals(email, actualAttributes.get("email"));
            assertEquals(nickname, actualAttributes.get("nickname"));
        }

        @Test
        void 사용자가_유효하지_않은_JWT로_WebSocket_연결_실패(){
            String invalidJwt = "invalid.jwt.token";

            when(jwtUtil.extractClaims(invalidJwt)).thenThrow(new JwtException("Invalid Token"));

            accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("Authorization","Bearer "+invalidJwt);
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

            assertDoesNotThrow(() -> {
                stompHandler.preSend(message, messageChannel);
            });

            verify(jwtUtil, times(1)).extractClaims(invalidJwt);
            assertTrue(accessor.getSessionAttributes().isEmpty());
        }

        @Test
        void 사용자가_JWT없이_WebSocket_연결_실패(){
            accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

            // Act & Assert
            MessagingException exception = assertThrows(MessagingException.class, () -> {
                stompHandler.preSend(message, messageChannel);
            });

            assertEquals("인증 헤더가 없거나 'Bearer ' 형식이 아닙니다.", exception.getMessage());
            verify(jwtUtil, never()).extractClaims(anyString());
        }
    }

    @Nested
    class SendTest{
        @Test
        void 사용자가_인증된_세션으로_메시지를_전송() {
            sessionAttributes.put("userId", 1L);

            accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

            assertDoesNotThrow(() -> {
                stompHandler.preSend(message, messageChannel);
            });
        }

        @Test
        void 사용자가_인증되지_않은_세션으로_메시지를_전송시_예외발생() {
            accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setSessionAttributes(sessionAttributes);
            Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());

            MessagingException exception = assertThrows(MessagingException.class, () -> {
                stompHandler.preSend(message, messageChannel);
            });

            assertEquals("세션 인증 정보 없음", exception.getMessage());
        }
    }
}
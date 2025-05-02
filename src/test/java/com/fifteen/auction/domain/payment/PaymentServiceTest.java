package com.fifteen.auction.domain.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.order.entity.Order;
import com.fifteen.auction.domain.order.repository.OrderRepository;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.PaymentCardResponse;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.entity.Payment;
import com.fifteen.auction.domain.payment.enums.PaymentStatus;
import com.fifteen.auction.domain.payment.repository.PaymentRepository;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.domain.payment.util.IdempotencyKeyGenerator;
import com.fifteen.auction.domain.payment.util.toss.TossFeignClient;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.PaymentFailException;
import com.fifteen.auction.global.dto.exception.ServerException;
import feign.FeignException;
import feign.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks PaymentService paymentService;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock TossFeignClient tossFeignClient;
    @Mock IdempotencyKeyGenerator keyGenerator;
    @Mock ObjectMapper objectMapper;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock ListOperations<String, String> listOps;

    @Nested @DisplayName("confirm 결제 요청")
    class ConfirmTests {

        @Test
        @DisplayName("정상 결제 승인 – paymentKey 검증")
        @MockitoSettings(strictness = Strictness.LENIENT)
        void confirmSuccess() {
            // lenient 캐시 stub
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
            lenient().when(valueOps.get(anyString())).thenReturn(null);

            String orderId        = "ORDER-123";
            String amount      = "12000";
            String idempotencyKey = "idem123123123";
            String paymentKey     = "PAYMENTKEY-123";

            Order order = mock(Order.class);
            given(order.getId()).willReturn(orderId);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(keyGenerator.generate()).willReturn(idempotencyKey);

            PaymentResponse tossResp = new PaymentResponse(
                    "CARD", paymentKey,
                    new PaymentCardResponse(12000L),
                    PaymentStatus.DONE,
                    orderId,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );
            given(tossFeignClient.confirmPayment(any(PaymentRequest.class), eq(idempotencyKey)))
                    .willReturn(tossResp);

            ConfirmResponse response = paymentService.confirm(
                    new PaymentRequest(orderId, amount, idempotencyKey),
                    1L
            );

            assertThat(response.getPaymentKey()).isEqualTo(paymentKey);
            assertThat(response.getAmount()).isEqualTo(12000L);
            then(tossFeignClient).should().confirmPayment(any(), eq(idempotencyKey));
            then(paymentRepository).should().save(argThat(p ->
                    paymentKey.equals(p.getPaymentKey())
            ));
        }

        @Test
        @DisplayName("주문 없음 예외")
        @MockitoSettings(strictness = Strictness.LENIENT)
        void confirmOrderNotFound() {
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
            lenient().when(valueOps.get(anyString())).thenReturn(null);

            given(orderRepository.findById(anyString()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    paymentService.confirm(
                            new PaymentRequest("order-123456", "1000", "KEY"),
                            1L
                    )
            )
                    .isInstanceOf(ClientException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("PG사 오류 시 PaymentFailException")
        @MockitoSettings(strictness = Strictness.LENIENT)
        void confirmPgError() {
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
            lenient().when(valueOps.get(anyString())).thenReturn(null);

            String idempotencyKey = "idem123123123";
            given(keyGenerator.generate()).willReturn(idempotencyKey);

            Order order = mock(Order.class);
            given(order.getId()).willReturn("ORD-1");
            given(orderRepository.findById(anyString()))
                    .willReturn(Optional.of(order));

            Request request = Request.create(
                    Request.HttpMethod.POST, "/payments",
                    Collections.emptyMap(), null,
                    Charset.defaultCharset(), null
            );
            given(tossFeignClient.confirmPayment(any(), eq(idempotencyKey)))
                    .willThrow(new FeignException.BadRequest(
                            "Bad Request", request, new byte[0], Collections.emptyMap()
                    ));

            assertThatThrownBy(() ->
                    paymentService.confirm(
                            new PaymentRequest("ORD-1", "1000", idempotencyKey),
                            1L
                    )
            ).isInstanceOf(PaymentFailException.class);
        }

        @Test @DisplayName("중복 결제 시 PAYMENT_ALREADY_PROCESSED 예외")
        void confirmIdempotencyCache() throws Exception {
            ValueOperations<String, String> redis = mock(ValueOperations.class);
            given(redisTemplate.opsForValue()).willReturn(redis);

            String cachedJson = "{\"paymentKey\":\"TXN-CACHED\",\"method\":\"CARD\",\"amount\":5000}";
            given(redis.get(anyString())).willReturn(cachedJson);

            ConfirmResponse cachedResponse = mock(ConfirmResponse.class);
            given(objectMapper.readValue(cachedJson, ConfirmResponse.class))
                    .willReturn(cachedResponse);

            ConfirmResponse response = paymentService.confirm(
                    new PaymentRequest("ORD-X","5000","IDEMP"),
                    1L
            );

            assertThat(response).isSameAs(cachedResponse);
            then(tossFeignClient).should(never())
                    .confirmPayment(any(), anyString());
        }
    }

    @Nested @DisplayName("receiveWebhook - DONE 상태")
    class DoneWebhookTests {

        @Test @DisplayName("DONE + existing → 조회만, 캐싱")
        void doneWhenFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);
            // opsForList 는 해당 테스트에서만 stub
            // given(redisTemplate.opsForList()).willReturn(listOps);

            String paymentKey = "PAYMENTKEY-123";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getStatus()).willReturn(PaymentStatus.DONE);

            Payment existing = mock(Payment.class);
            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.of(existing));

            paymentService.receiveWebhook(response);

            then(paymentRepository).should().findByPaymentKey(paymentKey);
            then(listOps).should(never()).leftPush(anyString(), anyString());
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "DONE");
            then(valueOps).should().set(cacheKey, "webhookConfirm", Duration.ofMinutes(5));
        }

        @Test @DisplayName("DONE + missing → 재시도 큐, 캐싱")
        void doneWhenNotFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);
            given(redisTemplate.opsForList()).willReturn(listOps);

            String paymentKey = "PAYMENTKEY-123";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getStatus()).willReturn(PaymentStatus.DONE);
            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.empty());

            paymentService.receiveWebhook(response);

            then(listOps).should().leftPush("webhook:retry:done", paymentKey);
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "DONE");
            then(valueOps).should().set(cacheKey, "webhookConfirm", Duration.ofMinutes(5));
        }
    }

    @Nested @DisplayName("receiveWebhook - EXPIRED 상태")
    class ExpiredWebhookTests {

        @Test @DisplayName("EXPIRED + missing → save, 캐싱")
        void expiredWhenNotFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            String paymentKey = "PAYMENTKEY-1236";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-EX");
            given(response.getStatus()).willReturn(PaymentStatus.EXPIRED);

            PaymentCardResponse card = mock(PaymentCardResponse.class);
            given(response.getCard()).willReturn(card);
            given(card.getAmount()).willReturn(10000L);
            given(response.getRequestedAt()).willReturn(OffsetDateTime.now());
            given(response.getApprovedAt()).willReturn(OffsetDateTime.now());

            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.empty());
            given(orderRepository.findById("ORDER-EX"))
                    .willReturn(Optional.of(mock(Order.class)));

            paymentService.receiveWebhook(response);

            then(paymentRepository).should().findByPaymentKey(paymentKey);
            then(orderRepository).should().findById("ORDER-EX");
            then(paymentRepository).should().save(any(Payment.class));
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "EXPIRED");
            then(valueOps).should().set(cacheKey, "webhookExpired", Duration.ofMinutes(5));
        }

        @Test @DisplayName("EXPIRED + existing → 예외 NO_MATCHED")
        void expiredWhenFound() {
            String paymentKey = "PAYMENTKEY-1235";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-EX2");
            given(response.getStatus()).willReturn(PaymentStatus.EXPIRED);

            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.of(mock(Payment.class)));

            assertThatThrownBy(() -> paymentService.receiveWebhook(response))
                    .isInstanceOf(ServerException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_WEBHOOK_UNMATCHED);
        }
    }

    @Nested @DisplayName("receiveWebhook - ABORTED 상태")
    class AbortedWebhookTests {

        @Test @DisplayName("ABORTED + missing → save, 캐싱")
        void abortedWhenNotFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            String paymentKey = "PAYMENTKEY-1234";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-AB");
            given(response.getStatus()).willReturn(PaymentStatus.ABORTED);

            PaymentCardResponse card = mock(PaymentCardResponse.class);
            given(response.getCard()).willReturn(card);
            given(card.getAmount()).willReturn(20000L);
            given(response.getRequestedAt()).willReturn(OffsetDateTime.now());
            given(response.getApprovedAt()).willReturn(OffsetDateTime.now());

            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.empty());
            given(orderRepository.findById("ORDER-AB"))
                    .willReturn(Optional.of(mock(Order.class)));

            paymentService.receiveWebhook(response);

            then(paymentRepository).should().findByPaymentKey(paymentKey);
            then(orderRepository).should().findById("ORDER-AB");
            then(paymentRepository).should().save(any(Payment.class));
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "ABORTED");
            then(valueOps).should().set(cacheKey, "webhookAborted", Duration.ofMinutes(5));
        }

        @Test @DisplayName("ABORTED + existing → 예외 NO_MATCHED")
        void abortedWhenFound() {
            String paymentKey = "PAYMENTKEY-1231";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-AB2");
            given(response.getStatus()).willReturn(PaymentStatus.ABORTED);

            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.of(mock(Payment.class)));

            assertThatThrownBy(() -> paymentService.receiveWebhook(response))
                    .isInstanceOf(ServerException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_WEBHOOK_UNMATCHED);
        }
    }

    @Nested @DisplayName("receiveWebhook - CANCEL 상태")
    class CancelWebhookTests {

        @Test @DisplayName("CANCEL + existing → cancel(), 캐싱")
        void cancelWhenFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            String paymentKey = "PAYMENTKEY-12312";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-C");
            given(response.getStatus()).willReturn(PaymentStatus.CANCELED);

            Payment existing = mock(Payment.class);
            given(existing.getStatus()).willReturn(PaymentStatus.DONE);
            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.of(existing));

            paymentService.receiveWebhook(response);

            then(paymentRepository).should().findByPaymentKey(paymentKey);
            then(existing).should().cancel();
            then(paymentRepository).should(never()).save(any());
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "CANCELED");
            then(valueOps).should().set(cacheKey, "webhookCancel", Duration.ofMinutes(5));
        }

        @Test @DisplayName("CANCEL + missing → save(), 캐싱")
        void cancelWhenNotFound() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);

            String paymentKey = "PAYMENTKEY-123123";
            PaymentResponse response = mock(PaymentResponse.class);
            given(response.getPaymentKey()).willReturn(paymentKey);
            given(response.getOrderId()).willReturn("ORDER-C2");
            given(response.getStatus()).willReturn(PaymentStatus.CANCELED);

            PaymentCardResponse card = mock(PaymentCardResponse.class);
            given(response.getCard()).willReturn(card);
            given(card.getAmount()).willReturn(40000L);
            given(response.getRequestedAt()).willReturn(OffsetDateTime.now());
            given(response.getApprovedAt()).willReturn(OffsetDateTime.now());

            given(paymentRepository.findByPaymentKey(paymentKey))
                    .willReturn(Optional.empty());
            given(orderRepository.findById("ORDER-C2"))
                    .willReturn(Optional.of(mock(Order.class)));

            paymentService.receiveWebhook(response);

            then(paymentRepository).should().findByPaymentKey(paymentKey);
            then(orderRepository).should().findById("ORDER-C2");
            then(paymentRepository).should().save(any(Payment.class));
            String cacheKey = "webhook:idempotency:" + DigestUtils.sha256Hex(paymentKey + "CANCELED");
            then(valueOps).should().set(cacheKey, "webhookCancel", Duration.ofMinutes(5));
        }
    }
}

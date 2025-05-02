package com.fifteen.auction.domain.payment.util.lock;

import com.fifteen.auction.domain.payment.dto.request.CancelPaymentRequest;
import com.fifteen.auction.domain.payment.dto.request.PaymentRequest;
import com.fifteen.auction.domain.payment.dto.response.ConfirmResponse;
import com.fifteen.auction.domain.payment.dto.response.PaymentResponse;
import com.fifteen.auction.domain.payment.service.PaymentService;
import com.fifteen.auction.infra.redis.lock.LockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentLockFacade {

    private final LockManager lockManager;
    private final PaymentService paymentService;

    public ConfirmResponse confirmPaymentWithLock(PaymentRequest request, Long userId) {
        String lockKey = "confirm:" + request.getPaymentKey();

        try {
            return lockManager.executeWithLock(lockKey, () ->
                    paymentService.confirm(request, userId)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        }
    }

    public void cancelPaymentWithLock(String paymentKey, CancelPaymentRequest dto, Long currentUserId) {
        String lockKey = "cancel:" + paymentKey;

        try {
            lockManager.executeWithLock(lockKey, () -> {
                paymentService.cancelPaymentByUser(paymentKey, dto, currentUserId);
                return null;
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        }
    }

    public void receiveWebhookWithLock(PaymentResponse dto) {
        String lockKey = "webhook_"+dto.getStatus()+":" + dto.getPaymentKey();
        try {
            lockManager.executeWithLock(lockKey, () -> {
                paymentService.receiveWebhook(dto);
                return null;
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        }
    }
}

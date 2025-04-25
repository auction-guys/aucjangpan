package com.fifteen.auction.infra.gMail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GmailSender {

    private final JavaMailSender mailSender;

    @Value("${settlement.email}")
    private String email;

    public void sendSettlement(String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("정산 내역 "+LocalDate.now());
        message.setText(body);
        mailSender.send(message);
    }

}

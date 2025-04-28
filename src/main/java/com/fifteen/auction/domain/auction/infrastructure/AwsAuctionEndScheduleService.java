package com.fifteen.auction.domain.auction.infrastructure;

import com.fifteen.auction.domain.auction.dto.event.AuctionOpenEvent;
import com.fifteen.auction.domain.auction.service.port.out.AuctionEndScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.DeleteScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.Target;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j @Service
@RequiredArgsConstructor
public class AwsAuctionEndScheduleService implements AuctionEndScheduleService {

    @Value("${aws-scheduler.arn.schedule-role}")
    private String scheduleRoleArn;

    @Value("${aws-scheduler.arn.sqs-queue}")
    private String sqsQueueArn;

    private final SchedulerClient client;

    public static final String RESERVE_MESSAGE_FORMAT =
            "{\"auctionId\": %d, \"auctionSeq\": %s, \"startPrice\": %d}";

    @Override
    public void scheduleAuctionEnd(AuctionOpenEvent e) {
        String scheduleName = "auction-end-" + e.getAuctionSeq();

        String reservedTime = DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneOffset.UTC)
                .format(e.getExpiresAt());

        String reserveMessage = String.format(RESERVE_MESSAGE_FORMAT,
                e.getAuctionId(), e.getAuctionSeq(), e.getStartPrice()
        );

        CreateScheduleRequest req = CreateScheduleRequest.builder()
                .name(scheduleName)
                .scheduleExpression(String.format("at(%s)", reservedTime))
                .flexibleTimeWindow(FlexibleTimeWindow.builder()
                        .mode("OFF")
                        .build())
                .target(Target.builder()
                        .arn(sqsQueueArn)
                        .roleArn(scheduleRoleArn)
                        .input(reserveMessage)
                        .build())
                .build();

        client.createSchedule(req);
        log.info("[경매 종료 이벤트 예약] [{}] auctionSeq={} sqsArn={} at={}", scheduleName, e.getAuctionSeq(), sqsQueueArn, reservedTime);
    }

    @Override
    public void cancelScheduleAuctionEnd(String auctionSeq) {
        String scheduleName = "auction-end-" + auctionSeq;
        DeleteScheduleRequest req = DeleteScheduleRequest.builder()
                .name(scheduleName)
                .groupName("default")
                .build();
        client.deleteSchedule(req);
        log.info("[경매 종료 이벤트 취소] [{}]", auctionSeq);
    }
}

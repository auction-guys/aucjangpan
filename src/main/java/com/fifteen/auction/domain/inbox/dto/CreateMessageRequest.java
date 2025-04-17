package com.fifteen.auction.domain.inbox.dto;

import com.fifteen.auction.domain.inbox.entity.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateMessageRequest {

    private Long destUserId;
    private MessageType type;
    private String message;

    private static final String PARTICIPANTS_MSG_FORMAT = "참여하신 '%s' 경매가 종료되었습니다.";
    private static final String WINNER_MSG_FORMAT = "'%s' 경매에 낙찰되셨습니다.";

    public static CreateMessageRequest forWinner(Long destUserId, String auctionSeq) {
        String winnerMsg = String.format(WINNER_MSG_FORMAT, auctionSeq);
        return new CreateMessageRequest(destUserId, MessageType.WINNER, winnerMsg);
    }

    public static CreateMessageRequest forParticipants(Long destUserId, String auctionSeq) {
        String winnerMsg = String.format(PARTICIPANTS_MSG_FORMAT, auctionSeq);
        return new CreateMessageRequest(destUserId, MessageType.PARTICIPANT, winnerMsg);
    }
}

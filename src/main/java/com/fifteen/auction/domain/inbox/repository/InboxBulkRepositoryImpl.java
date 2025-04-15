package com.fifteen.auction.domain.inbox.repository;

import com.fifteen.auction.domain.inbox.dto.CreateMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class InboxBulkRepositoryImpl implements InboxBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void sendAllMessages(List<CreateMessageRequest> messages) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO inbox(user_id, type, message) VALUES (?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        CreateMessageRequest request = messages.get(i);
                        ps.setLong(1, request.getDestUserId());
                        ps.setString(2, request.getType().name());
                        ps.setString(3, request.getMessage());
                    }

                    @Override
                    public int getBatchSize() {
                        return messages.size();
                    }
                }
        );
    }
}

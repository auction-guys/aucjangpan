package com.fifteen.auction.domain.auction.infrastructure;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

@RequiredArgsConstructor
public class QueueKeyResolver {
    private final int queueNum;

    public int fetchKey(String id) {
        CRC32 crc = new CRC32();
        crc.update(id.getBytes(StandardCharsets.UTF_8));
        return (int) (crc.getValue() % queueNum);
    }
}

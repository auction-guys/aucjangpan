package com.fifteen.auction.domain.settlement.util;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.util.List;

public class CsvUtil {

    public static <T extends RowMapper> void writeToCsv(String filePath, String[] headers, List<T> dataList) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 헤더 작성
            writer.writeNext(headers);
            // 데이터 작성
            for (T data : dataList) {
                writer.writeNext(data.toCsvRow());
            }
        } catch (Exception e) {
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }

    public static <T extends RowMapper> void writeToCsv(String filePath, String[] headers, T data) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // 헤더 작성
            writer.writeNext(headers);
            // 데이터 작성
            writer.writeNext(data.toCsvRow());
        } catch (Exception e) {
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }
}

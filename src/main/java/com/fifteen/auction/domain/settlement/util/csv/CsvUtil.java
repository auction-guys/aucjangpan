package com.fifteen.auction.domain.settlement.util.csv;

import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.infra.s3.S3Uploader;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvUtil {
    //TODO: 파일 이름이랑 밑에 static빠진거 나중에 다시 확인
    private final S3Uploader s3Uploader;

    private final static String[] headers = CsvConstants.SETTLEMENT_HEADERS;
    private final static String dir = "settlement";


    public <T extends RowMapper> String writeToCsv(List<T> dataList) {

        String fileName = "auto_settlement_" + LocalDate.now();
        File tempFile = null;

        try {
            // 임시 파일 생성
            tempFile = File.createTempFile(fileName, ".csv");

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile))) {
                // 헤더 작성
                writer.writeNext(headers);
                // 데이터 작성
                for (T data : dataList) {
                    writer.writeNext(data.toCsvRow());
                }
            }
            return s3Uploader.uploadCsv(tempFile, "text/csv", dir, fileName + ".csv");

        } catch (Exception e) {
            log.error("Settlement save error: ", e);
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        } finally {
            // 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public <T extends RowMapper> String writeToCsv(T data) {

        String fileName = "immediately_settlement_" + LocalDate.now();
        File tempFile = null;

        try {
            // 임시 파일 생성
            tempFile = File.createTempFile(fileName, ".csv");

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile))) {
                // 헤더 작성
                writer.writeNext(headers);
                // 데이터 작성
                writer.writeNext(data.toCsvRow());
            }
            return s3Uploader.uploadCsv(tempFile, "text/csv", dir, fileName + ".csv");

        } catch (Exception e) {
            log.error("Settlement save error: ", e);
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        } finally {
            // 임시 파일 삭제
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}

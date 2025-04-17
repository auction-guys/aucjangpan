package com.fifteen.auction.infra.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public String upload(MultipartFile multipartFile, String dir) {
        try {
            String originalName = multipartFile.getOriginalFilename();
            String ext = getFileExtension(originalName);
            String key = dir + "/" + UUID.randomUUID() + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucket, key, multipartFile.getInputStream(), metadata));
            return amazonS3.getUrl(bucket, key).toString();

        } catch (IOException e) {
            throw new ServerException(ErrorCode.UPLOAD_FAIL);
        }
    }

    // TODO: 나중에 인터페이스로 처리하거나 상속받아서 하는것도 나쁘지 않을듯
    public String uploadCsv(File file, String contentType, String dir, String fileName) {
        try {
            String key = dir + "/" + fileName;

            // 메모리에서 만들면 부하가 심할 것 같아 임시 파일을 작성해 보내는 방식으로 결정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            metadata.setContentType(contentType);

            PutObjectRequest putRequest = new PutObjectRequest(bucket, key, file);
            putRequest.setMetadata(metadata);

            amazonS3.putObject(putRequest);

            return amazonS3.getUrl(bucket, key).toString();
        } catch (Exception e) {
            throw new ServerException(ErrorCode.UPLOAD_FAIL);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            amazonS3.deleteObject(bucket, key);
        } catch (Exception e) {
            throw new ServerException(ErrorCode.DELETE_FAIL);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ServerException(ErrorCode.INVALID_EXTENSION);
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf("products/");
        if (index == -1) {
            throw new ServerException(ErrorCode.KEY_EXTRACTION_FAIL);
        }
        return fileUrl.substring(index);
    }
}
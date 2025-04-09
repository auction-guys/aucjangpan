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
            throw new ServerException(ErrorCode.S3_UPLOAD_FAIL, e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            amazonS3.deleteObject(bucket, key);
        } catch (Exception e) {
            throw new ServerException(ErrorCode.S3_DELETE_FAIL, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ServerException(ErrorCode.S3_INVALID_EXTENSION);
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractKeyFromUrl(String fileUrl) {
        int index = fileUrl.indexOf("products/");
        if (index == -1) {
            throw new ServerException(ErrorCode.S3_KEY_EXTRACTION_FAIL);
        }
        return fileUrl.substring(index);
    }
}
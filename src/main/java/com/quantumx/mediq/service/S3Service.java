package com.quantumx.mediq.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    private final String bucketName = "profile-pictures";

    public String uploadFile(String folder, MultipartFile file) {
        try {
            String fileName = folder + "/" + Instant.now().toEpochMilli() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}

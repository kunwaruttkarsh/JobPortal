package com.jobportal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.bucket-name}")
    private String bucketName;


    // upload resume
    public String uploadResume(MultipartFile file, String candidateEmail){
        validateFile(file);

        try{
            //generate unique key
            String key = "resumes/"+candidateEmail+ "/" + UUID.randomUUID()+ ".pdf";

            //upload to s3
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/pdf")
                    .contentDisposition("inline")
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromBytes(file.getBytes()));

            log.info("Resume uploaded to s3: {}", key);

            return key;

        } catch (IOException e) {
            log.error("Failed to upload resume: {}", e.getMessage() );
            throw new RuntimeException("Failed to upload resume");
        }
    }

    //generate presigned url 2 hour
    public String generatePresignedUrl(String key){
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(2))
                .getObjectRequest(r -> r
                        .bucket(bucketName)
                        .key(key))
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url().toString();
    }

    //delete resume from s3
    public void deleteResume(String key){
        if(key == null || key.isEmpty()) return;

        try{
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            log.info("Resume deleted from s3 : {}", key);
        } catch (Exception e) {
            log.error("Failed to delete resume: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {

        if(file == null || file.isEmpty()){
            throw new RuntimeException("File is Empty");
        }

        String contentType = file.getContentType();
        if(contentType == null || !contentType.equals("application/pdf")){
            throw new RuntimeException("Only PDF files are allowed");
        }

        long maxSize = 5 * 1024 *1024;
        if(file.getSize() > maxSize){
            throw new RuntimeException("File size must be less than 5MB");
        }
    }
}

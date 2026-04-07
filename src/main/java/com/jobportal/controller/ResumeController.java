package com.jobportal.controller;

import com.jobportal.repository.UserRepository;
import com.jobportal.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resume")
public class ResumeController {

    private final S3Service s3Service;
    private final UserRepository  userRepository;

    // upload resume@
    @PostMapping("/upload")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestParam("file") MultipartFile file){

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        String key = s3Service.uploadResume(file, email);
        String url = s3Service.generatePresignedUrl(key);

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully",
                "key" , key,
                "resumeUrl", url
        ));

    }

    // get fresh url for resume
    @GetMapping("/url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getResumeUrl(
            @RequestParam String key){
        String url = s3Service.generatePresignedUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }
}

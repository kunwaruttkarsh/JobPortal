
package com.jobportal.controller;

import com.jobportal.dto.request.*;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.service.impl.ApplicationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications APIs")
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;

    // Candidate applies for a job
    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply for a job")
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.apply(request));
    }

    // Candidate sees their applications
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get all applications")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                applicationService.getMyApplications(page, size));
    }

    // Recruiter sees applicants for a job
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get all applicants")
    public ResponseEntity<Page<ApplicationResponse>> getJobApplicants(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                applicationService.getJobApplicants(jobId, page, size));
    }

    // Recruiter updates status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update status of job application ")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(
                applicationService.updateStatus(id, request));
    }

    // Candidate withdraws application
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw application")
    public ResponseEntity<String> withdraw(@PathVariable Long id) {
        applicationService.withdraw(id);
        return ResponseEntity.ok("Application withdrawn successfully");
    }
}
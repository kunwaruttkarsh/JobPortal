package com.jobportal.service.impl;

import com.jobportal.dto.request.*;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.entity.*;
import com.jobportal.enums.ApplicationStatus;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.*;
import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .candidateName(app.getCandidate().getName())
                .candidateEmail(app.getCandidate().getEmail())
                .resumeUrl(app.getResumeUrl())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }

    // Candidate applies for a job
    public ApplicationResponse apply(ApplicationRequest req) {
        User candidate = getCurrentUser();

        Job job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.isActive())
            throw new BadRequestException("This job is no longer active");

        if (applicationRepository.existsByJobIdAndCandidateId(
                job.getId(), candidate.getId()))
            throw new BadRequestException("You have already applied for this job");

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resumeUrl(req.getResumeUrl())
                .build();

        // ✅ Send confirmation email
        emailService.sendStatusUpdate(
                candidate.getEmail(),
                candidate.getName(),
                job.getTitle(),
                job.getCompany(),
                ApplicationStatus.APPLIED
        );


        return toResponse(applicationRepository.save(application));
    }

    // Candidate sees their own applications
    public Page<ApplicationResponse> getMyApplications(int page, int size) {
        User candidate = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appliedAt").descending());
        return applicationRepository
                .findByCandidateId(candidate.getId(), pageable)
                .map(this::toResponse);
    }

    // Recruiter sees all applicants for their job
    public Page<ApplicationResponse> getJobApplicants(
            Long jobId, int page, int size) {
        User recruiter = getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getRecruiter().getId().equals(recruiter.getId()))
            throw new BadRequestException("Access denied");

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appliedAt").descending());
        return applicationRepository
                .findByJobId(jobId, pageable)
                .map(this::toResponse);
    }

    // Recruiter updates application status
    public ApplicationResponse updateStatus(
            Long applicationId, UpdateStatusRequest req) {

        User recruiter = getCurrentUser();

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found"));

        if (!application.getJob().getRecruiter()
                .getId().equals(recruiter.getId()))
            throw new BadRequestException("Access denied");

        application.setStatus(req.getStatus());

        //send email
        emailService.sendStatusUpdate(
                     application.getCandidate().getEmail(),
                     application.getCandidate().getName(),
                     application.getJob().getTitle(),
                     application.getJob().getCompany(),
                     req.getStatus()
        );

        return toResponse(applicationRepository.save(application));
    }

    // Candidate withdraws application
    public void withdraw(Long applicationId) {
        User candidate = getCurrentUser();

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found"));

        if (!application.getCandidate().getId().equals(candidate.getId()))
            throw new BadRequestException("Access denied");

        applicationRepository.delete(application);
    }
}
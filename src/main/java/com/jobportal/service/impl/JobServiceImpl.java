package com.jobportal.service.impl;

import com.jobportal.dto.request.JobRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobServiceImpl {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // Get currently logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Map entity to response DTO
    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .skills(job.getSkills())
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .recruiterName(job.getRecruiter().getName())
                .postedAt(job.getPostedAt())
                .build();
    }

    // Recruiter posts a job
    public JobResponse postJob(JobRequest req) {
        User recruiter = getCurrentUser();

        Job job = Job.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .company(req.getCompany())
                .location(req.getLocation())
                .skills(req.getSkills())
                .minSalary(req.getMinSalary())
                .maxSalary(req.getMaxSalary())
                .recruiter(recruiter)
                .active(true)
                .build();

        return toResponse(jobRepository.save(job));
    }

    // Anyone can search jobs
    public Page<JobResponse> searchJobs(String keyword,
                                        String location,
                                        Double minSalary,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository
                .searchJobs(keyword, location, minSalary, pageable)
                .map(this::toResponse);
    }

    // Get single job by ID
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return toResponse(job);
    }

    // Recruiter sees their own jobs
    public Page<JobResponse> getMyJobs(int page, int size) {
        User recruiter = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("postedAt").descending());
        return jobRepository
                .findByRecruiterIdAndActiveTrue(recruiter.getId(), pageable)
                .map(this::toResponse);
    }

    // Recruiter deletes their job
    public void deleteJob(Long id) {
        User recruiter = getCurrentUser();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getRecruiter().getId().equals(recruiter.getId()))
            throw new BadRequestException("You can only delete your own jobs");

        job.setActive(false);
        jobRepository.save(job);
    }
}
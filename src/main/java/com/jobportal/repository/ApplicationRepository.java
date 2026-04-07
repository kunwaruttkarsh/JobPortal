
package com.jobportal.repository;

import com.jobportal.entity.Application;
import com.jobportal.enums.ApplicationStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    // Candidate sees their applications
    Page<Application> findByCandidateId(
            Long candidateId, Pageable pageable);

    // Recruiter sees applicants for a job
    Page<Application> findByJobId(
            Long jobId, Pageable pageable);

    // Prevent duplicate applications
    boolean existsByJobIdAndCandidateId(
            Long jobId, Long candidateId);

    // Find specific application
    Optional<Application> findByIdAndJobId(
            Long id, Long jobId);
}
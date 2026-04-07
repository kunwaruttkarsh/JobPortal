package com.jobportal.dto.response;

import com.jobportal.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String candidateName;
    private String candidateEmail;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}

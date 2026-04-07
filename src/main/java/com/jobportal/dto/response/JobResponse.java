package com.jobportal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private String skills;
    private Double minSalary;
    private Double maxSalary;
    private String recruiterName;
    private LocalDateTime postedAt;
}
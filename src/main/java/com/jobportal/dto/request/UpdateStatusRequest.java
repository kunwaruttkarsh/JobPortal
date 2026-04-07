
package com.jobportal.dto.request;

import com.jobportal.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
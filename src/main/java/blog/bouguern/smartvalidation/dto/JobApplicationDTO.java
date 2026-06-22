package blog.bouguern.smartvalidation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobApplicationDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Professional summary is required")
    @Size(min = 50, max = 500, message = "Summary must be between 50 and 500 characters")
    private String professionalSummary;

    private String linkedInUrl;
}
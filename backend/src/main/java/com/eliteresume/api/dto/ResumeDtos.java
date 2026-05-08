package com.eliteresume.api.dto;

import com.eliteresume.api.entity.ScoreType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public class ResumeDtos {
    public record ResumeRequest(
            Long id,
            @NotBlank String firstName,
            String middleName,
            @NotBlank String lastName,
            String profileImagePath,
            String designation,
            @Email String email,
            String phone,
            String linkedIn,
            String github,
            String portfolio,
            String jobDescription,
            @Valid List<EducationRequest> education,
            @Valid List<SkillRequest> skills,
            @Valid List<ProjectRequest> projects,
            @Valid List<ExperienceRequest> experiences,
            @Valid List<CertificateRequest> certificates,
            @Valid List<LanguageRequest> languages
    ) {}

    public record EducationRequest(
            String school,
            String degree,
            Integer startYear,
            Integer endYear,
            ScoreType scoreType,
            String score
    ) {}

    public record SkillRequest(String name) {}

    public record ProjectRequest(
            String title,
            String startMonth,
            Integer startYear,
            String endMonth,
            Integer endYear,
            boolean present,
            List<String> bullets
    ) {}

    public record ExperienceRequest(
            String role,
            String organization,
            String startMonth,
            Integer startYear,
            String endMonth,
            Integer endYear,
            boolean present,
            List<String> bullets
    ) {}

    public record CertificateRequest(String name, String organization) {}

    public record LanguageRequest(String name) {}

    public record ResumeSummaryResponse(
            Long id,
            String fullName,
            String designation,
            Instant updatedAt,
            String pdfPath
    ) {}

    public record ResumeResponse(
            Long id,
            String firstName,
            String middleName,
            String lastName,
            String profileImagePath,
            String designation,
            String email,
            String phone,
            String linkedIn,
            String github,
            String portfolio,
            String jobDescription,
            String careerObjective,
            String pdfPath,
            List<EducationResponse> education,
            List<SkillResponse> skills,
            List<ProjectResponse> projects,
            List<ExperienceResponse> experiences,
            List<CertificateResponse> certificates,
            List<LanguageResponse> languages
    ) {}

    public record EducationResponse(String school, String degree, Integer startYear, Integer endYear, ScoreType scoreType, String score) {}
    public record SkillResponse(String name) {}
    public record ProjectResponse(String title, String startMonth, Integer startYear, String endMonth, Integer endYear, boolean present, List<String> bullets) {}
    public record ExperienceResponse(String role, String organization, String startMonth, Integer startYear, String endMonth, Integer endYear, boolean present, List<String> bullets) {}
    public record CertificateResponse(String name, String organization) {}
    public record LanguageResponse(String name) {}
}

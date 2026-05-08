package com.eliteresume.api.service;

import com.eliteresume.api.dto.ResumeDtos;
import com.eliteresume.api.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResumeMapper {
    public ResumeDtos.ResumeSummaryResponse toSummary(Resume resume) {
        return new ResumeDtos.ResumeSummaryResponse(
                resume.getId(),
                join(" ", resume.getFirstName(), resume.getMiddleName(), resume.getLastName()),
                resume.getDesignation(),
                resume.getUpdatedAt(),
                resume.getPdfPath()
        );
    }

    public ResumeDtos.ResumeResponse toResponse(Resume resume) {
        return new ResumeDtos.ResumeResponse(
                resume.getId(),
                resume.getFirstName(),
                resume.getMiddleName(),
                resume.getLastName(),
                resume.getProfileImagePath(),
                resume.getDesignation(),
                resume.getEmail(),
                resume.getPhone(),
                resume.getLinkedIn(),
                resume.getGithub(),
                resume.getPortfolio(),
                resume.getJobDescription(),
                resume.getCareerObjective(),
                resume.getPdfPath(),
                resume.getEducation().stream().map(this::toEducation).toList(),
                resume.getSkills().stream().map(skill -> new ResumeDtos.SkillResponse(skill.getName())).toList(),
                resume.getProjects().stream().map(this::toProject).toList(),
                resume.getExperiences().stream().map(this::toExperience).toList(),
                resume.getCertificates().stream().map(c -> new ResumeDtos.CertificateResponse(c.getName(), c.getOrganization())).toList(),
                resume.getLanguages().stream().map(l -> new ResumeDtos.LanguageResponse(l.getName())).toList()
        );
    }

    public void applyRequest(Resume resume, ResumeDtos.ResumeRequest request) {
        resume.setFirstName(request.firstName());
        resume.setMiddleName(request.middleName());
        resume.setLastName(request.lastName());
        resume.setProfileImagePath(request.profileImagePath());
        resume.setDesignation(request.designation());
        resume.setEmail(request.email());
        resume.setPhone(request.phone());
        resume.setLinkedIn(request.linkedIn());
        resume.setGithub(request.github());
        resume.setPortfolio(request.portfolio());
        resume.setJobDescription(request.jobDescription());

        resume.getEducation().clear();
        for (int i = 0; i < safe(request.education()).size(); i++) {
            var source = request.education().get(i);
            Education education = new Education();
            education.setResume(resume);
            education.setSchool(source.school());
            education.setDegree(source.degree());
            education.setStartYear(source.startYear());
            education.setEndYear(source.endYear());
            education.setScoreType(source.scoreType());
            education.setScore(source.score());
            education.setSortOrder(i);
            resume.getEducation().add(education);
        }

        resume.getSkills().clear();
        for (int i = 0; i < safe(request.skills()).size(); i++) {
            Skill skill = new Skill();
            skill.setResume(resume);
            skill.setName(request.skills().get(i).name());
            skill.setSortOrder(i);
            resume.getSkills().add(skill);
        }

        resume.getProjects().clear();
        for (int i = 0; i < safe(request.projects()).size(); i++) {
            var source = request.projects().get(i);
            Project project = new Project();
            project.setResume(resume);
            project.setTitle(source.title());
            project.setStartMonth(source.startMonth());
            project.setStartYear(source.startYear());
            project.setEndMonth(source.endMonth());
            project.setEndYear(source.endYear());
            project.setPresent(source.present());
            project.setBullets(new ArrayList<>(safe(source.bullets())));
            project.setSortOrder(i);
            resume.getProjects().add(project);
        }

        resume.getExperiences().clear();
        for (int i = 0; i < safe(request.experiences()).size(); i++) {
            var source = request.experiences().get(i);
            Experience experience = new Experience();
            experience.setResume(resume);
            experience.setRole(source.role());
            experience.setOrganization(source.organization());
            experience.setStartMonth(source.startMonth());
            experience.setStartYear(source.startYear());
            experience.setEndMonth(source.endMonth());
            experience.setEndYear(source.endYear());
            experience.setPresent(source.present());
            experience.setBullets(new ArrayList<>(safe(source.bullets())));
            experience.setSortOrder(i);
            resume.getExperiences().add(experience);
        }

        resume.getCertificates().clear();
        for (int i = 0; i < safe(request.certificates()).size(); i++) {
            var source = request.certificates().get(i);
            Certificate certificate = new Certificate();
            certificate.setResume(resume);
            certificate.setName(source.name());
            certificate.setOrganization(source.organization());
            certificate.setSortOrder(i);
            resume.getCertificates().add(certificate);
        }

        resume.getLanguages().clear();
        for (int i = 0; i < safe(request.languages()).size(); i++) {
            Language language = new Language();
            language.setResume(resume);
            language.setName(request.languages().get(i).name());
            language.setSortOrder(i);
            resume.getLanguages().add(language);
        }
    }

    private ResumeDtos.EducationResponse toEducation(Education education) {
        return new ResumeDtos.EducationResponse(
                education.getSchool(),
                education.getDegree(),
                education.getStartYear(),
                education.getEndYear(),
                education.getScoreType(),
                education.getScore()
        );
    }

    private ResumeDtos.ProjectResponse toProject(Project project) {
        return new ResumeDtos.ProjectResponse(
                project.getTitle(),
                project.getStartMonth(),
                project.getStartYear(),
                project.getEndMonth(),
                project.getEndYear(),
                project.isPresent(),
                project.getBullets()
        );
    }

    private ResumeDtos.ExperienceResponse toExperience(Experience experience) {
        return new ResumeDtos.ExperienceResponse(
                experience.getRole(),
                experience.getOrganization(),
                experience.getStartMonth(),
                experience.getStartYear(),
                experience.getEndMonth(),
                experience.getEndYear(),
                experience.isPresent(),
                experience.getBullets()
        );
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String join(String delimiter, String... values) {
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + delimiter + right)
                .orElse("");
    }
}

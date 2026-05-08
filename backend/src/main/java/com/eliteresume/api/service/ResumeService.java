package com.eliteresume.api.service;

import com.eliteresume.api.dto.ResumeDtos;
import com.eliteresume.api.entity.Resume;
import com.eliteresume.api.exception.ApiException;
import com.eliteresume.api.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final CurrentUserService currentUserService;
    private final ResumeMapper resumeMapper;
    private final GeminiService geminiService;
    private final ResumePdfService resumePdfService;

    @Transactional(readOnly = true)
    public List<ResumeDtos.ResumeSummaryResponse> listCurrentUserResumes() {
        var user = currentUserService.getCurrentUser();
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(resumeMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeDtos.ResumeResponse getResume(Long id) {
        var user = currentUserService.getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
        return resumeMapper.toResponse(resume);
    }

    @Transactional
    public ResumeDtos.ResumeResponse saveDraft(ResumeDtos.ResumeRequest request) {
        Resume resume = resolveResume(request);
        resumeMapper.applyRequest(resume, request);
        return resumeMapper.toResponse(resumeRepository.save(resume));
    }

    @Transactional
    public ResumeDtos.ResumeResponse generate(ResumeDtos.ResumeRequest request) {
        Resume resume = resolveResume(request);
        resumeMapper.applyRequest(resume, request);

        var enhancement = geminiService.enhance(request);
        resume.setCareerObjective(enhancement.careerObjective());
        applyEnhancedProjectBullets(resume, enhancement.projectBullets());
        applyEnhancedExperienceBullets(resume, enhancement.experienceBullets());

        Resume saved = resumeRepository.save(resume);
        saved.setPdfPath(resumePdfService.generatePdf(saved));
        return resumeMapper.toResponse(resumeRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public Resource download(Long id) {
        var user = currentUserService.getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
        if (!StringUtils.hasText(resume.getPdfPath())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Resume PDF has not been generated yet");
        }
        Path path = Path.of(resume.getPdfPath()).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Stored resume PDF file is missing");
        }
        return new FileSystemResource(path);
    }

    private Resume resolveResume(ResumeDtos.ResumeRequest request) {
        var user = currentUserService.getCurrentUser();
        if (request.id() == null) {
            Resume resume = new Resume();
            resume.setUser(user);
            return resume;
        }
        return resumeRepository.findByIdAndUserId(request.id(), user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
    }

    private void applyEnhancedProjectBullets(Resume resume, List<List<String>> groups) {
        for (int i = 0; i < resume.getProjects().size() && i < groups.size(); i++) {
            resume.getProjects().get(i).setBullets(new ArrayList<>(groups.get(i)));
        }
    }

    private void applyEnhancedExperienceBullets(Resume resume, List<List<String>> groups) {
        for (int i = 0; i < resume.getExperiences().size() && i < groups.size(); i++) {
            resume.getExperiences().get(i).setBullets(new ArrayList<>(groups.get(i)));
        }
    }
}

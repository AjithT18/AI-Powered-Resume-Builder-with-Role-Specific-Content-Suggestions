package com.eliteresume.api.controller;

import com.eliteresume.api.dto.ResumeDtos;
import com.eliteresume.api.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping
    public List<ResumeDtos.ResumeSummaryResponse> list() {
        return resumeService.listCurrentUserResumes();
    }

    @GetMapping("/{id}")
    public ResumeDtos.ResumeResponse get(@PathVariable Long id) {
        return resumeService.getResume(id);
    }

    @PostMapping
    public ResumeDtos.ResumeResponse saveDraft(@Valid @RequestBody ResumeDtos.ResumeRequest request) {
        return resumeService.saveDraft(request);
    }

    @PostMapping("/generate")
    public ResumeDtos.ResumeResponse generate(@Valid @RequestBody ResumeDtos.ResumeRequest request) {
        return resumeService.generate(request);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = resumeService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

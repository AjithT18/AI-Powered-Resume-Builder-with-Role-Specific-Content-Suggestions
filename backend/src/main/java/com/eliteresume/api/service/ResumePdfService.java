package com.eliteresume.api.service;

import com.eliteresume.api.config.AppProperties;
import com.eliteresume.api.entity.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumePdfService {
    private final AppProperties appProperties;

    private static final Font NAME_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
    private static final Font LINK_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLUE);

    public String generatePdf(Resume resume) {
        try {
            Path dir = Path.of(appProperties.storage().resumeDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String safeName = fullName(resume).replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("(^-|-$)", "");
            Path output = dir.resolve((safeName.isBlank() ? "resume" : safeName) + "-" + Instant.now().toEpochMilli() + ".pdf");

            Document document = new Document(PageSize.A4, 38, 38, 28, 28);
            PdfWriter.getInstance(document, new FileOutputStream(output.toFile()));
            document.open();

            addHeader(document, resume);
            addObjective(document, resume);
            addEducation(document, resume.getEducation());
            addSkills(document, resume.getSkills());
            addProjects(document, resume.getProjects());
            addExperience(document, resume.getExperiences());
            addCertificates(document, resume.getCertificates());
            addLanguages(document, resume.getLanguages());

            document.close();
            return output.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate resume PDF", exception);
        }
    }

    private void addHeader(Document document, Resume resume) throws DocumentException {
        Paragraph name = new Paragraph(fullName(resume).toUpperCase(), NAME_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingAfter(4);
        document.add(name);

        Paragraph designation = new Paragraph(text(resume.getDesignation()), BODY_FONT);
        designation.setAlignment(Element.ALIGN_CENTER);
        designation.setSpacingAfter(2);
        if (StringUtils.hasText(resume.getDesignation())) {
            document.add(designation);
        }

        Paragraph contact = new Paragraph(joinNonBlank(" | ",
                resume.getPhone(),
                resume.getEmail(),
                resume.getLinkedIn(),
                resume.getGithub(),
                resume.getPortfolio()
        ), LINK_FONT);
        contact.setAlignment(Element.ALIGN_CENTER);
        contact.setSpacingAfter(8);
        document.add(contact);
    }

    private void addObjective(Document document, Resume resume) throws DocumentException {
        if (StringUtils.hasText(resume.getCareerObjective())) {
            Paragraph objective = new Paragraph(resume.getCareerObjective(), BODY_FONT);
            objective.setAlignment(Element.ALIGN_JUSTIFIED);
            objective.setSpacingAfter(6);
            document.add(objective);
        }
    }

    private void addEducation(Document document, List<Education> education) throws DocumentException {
        if (education.isEmpty()) return;
        addSectionTitle(document, "EDUCATION");
        for (Education item : education) {
            Paragraph line = new Paragraph();
            line.add(new Chunk(text(item.getSchool()), BOLD_FONT));
            line.add(new Chunk("    " + yearRange(item.getStartYear(), item.getEndYear()), BOLD_FONT));
            document.add(line);
            document.add(new Paragraph(joinNonBlank(" | ", item.getDegree(), score(item)), BODY_FONT));
        }
    }

    private void addSkills(Document document, List<Skill> skills) throws DocumentException {
        if (skills.isEmpty()) return;
        addSectionTitle(document, "TECHNICAL SKILLS");
        document.add(new Paragraph(skills.stream().map(Skill::getName).filter(StringUtils::hasText).collect(Collectors.joining(", ")), BODY_FONT));
    }

    private void addProjects(Document document, List<Project> projects) throws DocumentException {
        if (projects.isEmpty()) return;
        addSectionTitle(document, "PROJECTS");
        for (Project project : projects) {
            document.add(new Paragraph(text(project.getTitle()) + " : " + dateRange(project), BOLD_FONT));
            addBullets(document, project.getBullets());
        }
    }

    private void addExperience(Document document, List<Experience> experiences) throws DocumentException {
        if (experiences.isEmpty()) return;
        addSectionTitle(document, "EXPERIENCE");
        for (Experience experience : experiences) {
            document.add(new Paragraph(joinNonBlank(" at ", experience.getRole(), experience.getOrganization()) + " - " + dateRange(experience), BOLD_FONT));
            addBullets(document, experience.getBullets());
        }
    }

    private void addCertificates(Document document, List<Certificate> certificates) throws DocumentException {
        if (certificates.isEmpty()) return;
        addSectionTitle(document, "CERTIFICATES");
        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 8);
        for (Certificate certificate : certificates) {
            list.add(new ListItem(joinNonBlank(" - ", certificate.getName(), certificate.getOrganization()), BODY_FONT));
        }
        document.add(list);
    }

    private void addLanguages(Document document, List<Language> languages) throws DocumentException {
        if (languages.isEmpty()) return;
        addSectionTitle(document, "LANGUAGES");
        document.add(new Paragraph(languages.stream().map(Language::getName).filter(StringUtils::hasText).collect(Collectors.joining(", ")), BODY_FONT));
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, SECTION_FONT);
        paragraph.setSpacingBefore(7);
        paragraph.setSpacingAfter(2);
        document.add(paragraph);
        document.add(new LineSeparator());
    }

    private void addBullets(Document document, java.util.List<String> bullets) throws DocumentException {
        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 10);
        for (String bullet : bullets) {
            if (StringUtils.hasText(bullet)) {
                list.add(new ListItem(bullet, BODY_FONT));
            }
        }
        document.add(list);
    }

    private String fullName(Resume resume) {
        return joinNonBlank(" ", resume.getFirstName(), resume.getMiddleName(), resume.getLastName());
    }

    private String dateRange(Project project) {
        return joinNonBlank(" ", project.getStartMonth(), year(project.getStartYear())) + " - "
                + (project.isPresent() ? "Present" : joinNonBlank(" ", project.getEndMonth(), year(project.getEndYear())));
    }

    private String dateRange(Experience experience) {
        return joinNonBlank(" ", experience.getStartMonth(), year(experience.getStartYear())) + " - "
                + (experience.isPresent() ? "Present" : joinNonBlank(" ", experience.getEndMonth(), year(experience.getEndYear())));
    }

    private String yearRange(Integer start, Integer end) {
        return joinNonBlank(" - ", year(start), year(end));
    }

    private String year(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String score(Education item) {
        if (item.getScoreType() == null || !StringUtils.hasText(item.getScore())) {
            return "";
        }
        return item.getScoreType().name().substring(0, 1) + item.getScoreType().name().substring(1).toLowerCase() + ": " + item.getScore();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private String joinNonBlank(String delimiter, String... values) {
        return java.util.Arrays.stream(values)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(delimiter));
    }
}

package com.eliteresume.api.service;

import com.eliteresume.api.config.AppProperties;
import com.eliteresume.api.dto.GeminiDtos;
import com.eliteresume.api.dto.ResumeDtos;
import com.eliteresume.api.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final Pattern TRAILING_COMMAS = Pattern.compile(",\\s*([}\\]])");
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9+#.\\-]{2,}");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "this", "that", "from", "your", "you", "are", "our", "role", "team", "will",
            "have", "has", "had", "into", "using", "use", "used", "but", "not", "can", "able", "all", "any", "per",
            "job", "work", "years", "year", "experience", "skills", "skill", "requirements", "responsibilities",
            "strong", "good", "best", "new", "other", "their", "them", "about", "across", "within", "through"
    );
    private final AppProperties appProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiDtos.GeminiEnhancement enhance(ResumeDtos.ResumeRequest request) {
        String apiKey = textOr(appProperties.gemini().apiKey(), "").trim();
        if (!StringUtils.hasText(apiKey) || looksLikeUnresolvedPlaceholder(apiKey)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Gemini API key is missing. Set GEMINI_API_KEY before generating a resume.");
        }

        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPrompt(request))))),
                "generationConfig", Map.of("temperature", 0.35)
        );

        Set<String> modelCandidates = new LinkedHashSet<>();
        modelCandidates.add(textOr(appProperties.gemini().model(), ""));
        modelCandidates.add("gemini-1.5-flash");
        modelCandidates.add("gemini-1.5-flash-8b");

        List<String> apiVersions = List.of("v1beta", "v1");
        String lastErrorDetail = "Unknown Gemini API error";
        String rawAiText = "";
        for (String apiVersion : apiVersions) {
            for (String model : modelCandidates) {
                if (!StringUtils.hasText(model)) {
                    continue;
                }
                String endpoint = UriComponentsBuilder
                        .fromHttpUrl("https://generativelanguage.googleapis.com/" + apiVersion + "/models/" + model + ":generateContent")
                        .queryParam("key", apiKey)
                        .build()
                        .toUriString();
                try {
                    String response = webClient.post()
                            .uri(endpoint)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    GeminiDtos.GeminiEnhancement parsed = parseGeminiResponse(response, request);
                    return polishEnhancement(normalizeCounts(parsed, request), request);
                } catch (WebClientResponseException exception) {
                    String detail = extractErrorDetail(exception.getResponseBodyAsString(), exception.getMessage());
                    lastErrorDetail = detail;
                    log.warn("Gemini endpoint '{}:{}' failed with status {}: {}", apiVersion, model, exception.getStatusCode(), detail);
                    if (!isModelCompatibilityError(exception.getStatusCode().value(), detail)) {
                        rawAiText = detail;
                    }
                } catch (Exception exception) {
                    lastErrorDetail = exception.getMessage();
                    log.warn("Gemini enhancement failed for endpoint '{}:{}': {}", apiVersion, model, exception.getMessage());
                    rawAiText = exception.getMessage();
                }
            }
        }
        log.warn("All Gemini endpoints failed, using local fallback enhancement. Last error: {}", lastErrorDetail);
        return polishEnhancement(normalizeCounts(fromPlainText(rawAiText, request), request), request);
    }

    private String buildPrompt(ResumeDtos.ResumeRequest request) {
        return """
                You are EliteResume, a professional resume writing assistant.
                Tailor the output to the job description and improve wording for ATS-friendly clarity.
                Return ONLY valid JSON with this exact shape:
                {
                  "careerObjective": "2-3 concise lines",
                  "projectBullets": [["same count as project 1 input"], ["same count as project 2 input"]],
                  "experienceBullets": [["same count as experience 1 input"]]
                }

                Strict rules:
                - Preserve the same number of bullet points for every project and experience entry.
                - Do not invent credentials, dates, companies, metrics, or tools not implied by the user data.
                - Keep bullets action-oriented, specific, and professional (12-24 words each).
                - Every bullet should begin with a strong action verb.
                - Include relevant job-description keywords naturally in objective and bullets.
                - Avoid generic wording like "worked on", "helped with", or "responsible for".
                - Career objective must be exactly 2-3 sentences and role-targeted.
                - Return only pure JSON. No markdown, no code fences, no explanations.

                Candidate resume JSON:
                %s

                Job description to optimize for:
                %s
                """.formatted(toJson(request), textOr(request.jobDescription(), "No job description provided."));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return "{}";
        }
    }

    private GeminiDtos.GeminiEnhancement parseGeminiResponse(String response, ResumeDtos.ResumeRequest request) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode partsNode = root.path("candidates").path(0).path("content").path("parts");
        StringBuilder textBuilder = new StringBuilder();
        if (partsNode.isArray()) {
            for (JsonNode part : partsNode) {
                String textPart = part.path("text").asText();
                if (StringUtils.hasText(textPart)) {
                    textBuilder.append(textPart);
                }
            }
        }

        String text = textBuilder.toString();
        if (!StringUtils.hasText(text)) {
            return new GeminiDtos.GeminiEnhancement(defaultObjective(request), List.of(), List.of());
        }

        String jsonPayload = extractJson(text);
        JsonNode enhancementNode = readEnhancementNode(jsonPayload, text);
        String careerObjective = firstText(enhancementNode, "careerObjective", "objective", "career_objective");
        List<List<String>> projectBullets = readBulletGroups(
                enhancementNode,
                List.of("projectBullets", "projects", "project_bullets")
        );
        List<List<String>> experienceBullets = readBulletGroups(
                enhancementNode,
                List.of("experienceBullets", "experiences", "experience_bullets")
        );

        if (!StringUtils.hasText(careerObjective) && projectBullets.isEmpty() && experienceBullets.isEmpty()) {
            return fromPlainText(text, request);
        }
        return new GeminiDtos.GeminiEnhancement(careerObjective, projectBullets, experienceBullets);
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private boolean isModelCompatibilityError(int statusCode, String detail) {
        String message = textOr(detail, "").toLowerCase();
        return statusCode == 404
                || message.contains("model")
                || message.contains("not found")
                || message.contains("not supported");
    }

    private String extractErrorDetail(String responseBody, String fallbackMessage) {
        try {
            if (StringUtils.hasText(responseBody)) {
                JsonNode node = objectMapper.readTree(responseBody);
                String apiMessage = node.path("error").path("message").asText();
                if (StringUtils.hasText(apiMessage)) {
                    return apiMessage;
                }
            }
        } catch (Exception ignored) {
            // Return fallback below if the response body is not JSON.
        }
        return StringUtils.hasText(responseBody) ? responseBody : fallbackMessage;
    }

    private JsonNode readEnhancementNode(String jsonPayload, String rawText) throws Exception {
        try {
            return objectMapper.readTree(jsonPayload);
        } catch (Exception first) {
            String normalized = jsonPayload
                    .replace('“', '"')
                    .replace('”', '"')
                    .replace('’', '\'')
                    .replace('`', '"');
            normalized = TRAILING_COMMAS.matcher(normalized).replaceAll("$1");
            try {
                return objectMapper.readTree(normalized);
            } catch (Exception second) {
                log.warn("Gemini JSON parse fallback used. Raw text preview: {}", rawText.substring(0, Math.min(320, rawText.length())));
                return objectMapper.createObjectNode();
            }
        }
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.path(key);
            if (!value.isMissingNode() && !value.isNull()) {
                String text = value.asText();
                if (StringUtils.hasText(text)) {
                    return text.trim();
                }
            }
        }
        return "";
    }

    private List<List<String>> readBulletGroups(JsonNode node, List<String> keys) {
        JsonNode groupsNode = null;
        for (String key : keys) {
            JsonNode candidate = node.path(key);
            if (!candidate.isMissingNode() && !candidate.isNull()) {
                groupsNode = candidate;
                break;
            }
        }
        if (groupsNode == null) {
            return List.of();
        }

        List<List<String>> result = new ArrayList<>();
        if (groupsNode.isArray()) {
            for (JsonNode group : groupsNode) {
                result.add(readSingleGroup(group));
            }
            return result;
        }

        if (groupsNode.isObject()) {
            groupsNode.fields().forEachRemaining(entry -> result.add(readSingleGroup(entry.getValue())));
        }
        return result;
    }

    private List<String> readSingleGroup(JsonNode groupNode) {
        if (groupNode == null || groupNode.isNull() || groupNode.isMissingNode()) {
            return List.of();
        }

        if (groupNode.isArray()) {
            List<String> bullets = new ArrayList<>();
            for (JsonNode bulletNode : groupNode) {
                String bullet = bulletNode.asText("");
                if (StringUtils.hasText(bullet)) {
                    bullets.add(cleanBullet(bullet));
                }
            }
            return bullets;
        }

        String text = groupNode.asText("");
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String[] lines = text.split("\\r?\\n");
        List<String> bullets = new ArrayList<>();
        for (String line : lines) {
            String cleaned = cleanBullet(line);
            if (StringUtils.hasText(cleaned)) {
                bullets.add(cleaned);
            }
        }
        return bullets;
    }

    private String cleanBullet(String value) {
        String cleaned = value == null ? "" : value.trim();
        cleaned = cleaned.replaceFirst("^[\\-\\*\\u2022\\d\\.\\)\\s]+", "").trim();
        return cleaned;
    }

    private GeminiDtos.GeminiEnhancement fromPlainText(String text, ResumeDtos.ResumeRequest request) {
        List<String> lines = text.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        String objective = lines.isEmpty() ? defaultObjective(request) : lines.get(0);
        List<String> bulletLines = lines.stream()
                .filter(line -> line.startsWith("-") || line.startsWith("*") || line.matches("^\\d+[\\.)].*"))
                .map(this::cleanBullet)
                .filter(StringUtils::hasText)
                .toList();

        if (bulletLines.isEmpty()) {
            return new GeminiDtos.GeminiEnhancement(objective, List.of(), List.of());
        }

        List<ResumeDtos.ProjectRequest> projects = safe(request.projects());
        List<ResumeDtos.ExperienceRequest> experiences = safe(request.experiences());
        List<List<String>> projectGroups = new ArrayList<>();
        int projectCount = projects.size();
        int experienceCount = experiences.size();

        int cursor = 0;
        for (int i = 0; i < projectCount; i++) {
            int size = Math.max(1, safe(projects.get(i).bullets()).size());
            List<String> group = new ArrayList<>();
            for (int j = 0; j < size && cursor < bulletLines.size(); j++) {
                group.add(bulletLines.get(cursor++));
            }
            projectGroups.add(group);
        }

        List<List<String>> experienceGroups = new ArrayList<>();
        for (int i = 0; i < experienceCount; i++) {
            int size = Math.max(1, safe(experiences.get(i).bullets()).size());
            List<String> group = new ArrayList<>();
            for (int j = 0; j < size && cursor < bulletLines.size(); j++) {
                group.add(bulletLines.get(cursor++));
            }
            experienceGroups.add(group);
        }

        return new GeminiDtos.GeminiEnhancement(objective, projectGroups, experienceGroups);
    }

    private GeminiDtos.GeminiEnhancement normalizeCounts(GeminiDtos.GeminiEnhancement enhancement, ResumeDtos.ResumeRequest request) {
        return new GeminiDtos.GeminiEnhancement(
                StringUtils.hasText(enhancement.careerObjective()) ? enhancement.careerObjective() : defaultObjective(request),
                normalizeBulletGroups(enhancement.projectBullets(), projectInputs(request), projectTitles(request)),
                normalizeBulletGroups(enhancement.experienceBullets(), experienceInputs(request), experienceTitles(request))
        );
    }

    private GeminiDtos.GeminiEnhancement polishEnhancement(GeminiDtos.GeminiEnhancement enhancement, ResumeDtos.ResumeRequest request) {
        List<String> jdKeywords = extractTopKeywords(request.jobDescription(), 8);
        String objective = polishObjective(enhancement.careerObjective(), request, jdKeywords);
        List<List<String>> projects = polishBulletGroups(enhancement.projectBullets(), jdKeywords);
        List<List<String>> experiences = polishBulletGroups(enhancement.experienceBullets(), jdKeywords);
        return new GeminiDtos.GeminiEnhancement(objective, projects, experiences);
    }

    private String polishObjective(String objective, ResumeDtos.ResumeRequest request, List<String> jdKeywords) {
        String base = StringUtils.hasText(objective) ? objective.trim() : defaultObjective(request);
        List<String> sentences = SENTENCE_SPLIT.splitAsStream(base)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(ArrayList::new));
        if (sentences.isEmpty()) {
            sentences.add(defaultObjective(request));
        }
        if (sentences.size() > 3) {
            sentences = new ArrayList<>(sentences.subList(0, 3));
        }
        while (sentences.size() < 2) {
            String role = textOr(request.designation(), "software role");
            sentences.add("Focused on delivering measurable impact in a " + role + " through reliable and scalable execution.");
        }

        String joined = String.join(" ", sentences);
        String joinedLower = joined.toLowerCase();
        if (jdKeywords.size() >= 2 && jdKeywords.stream().noneMatch(k -> joinedLower.contains(k.toLowerCase()))) {
            joined = joined + " Strong alignment with " + jdKeywords.get(0) + " and " + jdKeywords.get(1) + " requirements.";
        }
        return joined.trim();
    }

    private List<List<String>> polishBulletGroups(List<List<String>> groups, List<String> jdKeywords) {
        List<List<String>> polished = new ArrayList<>();
        for (List<String> group : safe(groups)) {
            List<String> cleanedGroup = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (String bullet : safe(group)) {
                String cleaned = polishBullet(bullet, jdKeywords);
                String key = cleaned.toLowerCase();
                if (StringUtils.hasText(cleaned) && !seen.contains(key)) {
                    seen.add(key);
                    cleanedGroup.add(cleaned);
                }
            }
            polished.add(cleanedGroup);
        }
        return polished;
    }

    private String polishBullet(String bullet, List<String> jdKeywords) {
        String cleaned = cleanBullet(bullet);
        if (!StringUtils.hasText(cleaned)) {
            return cleaned;
        }
        String lower = cleaned.toLowerCase();
        if (lower.startsWith("worked on ") || lower.startsWith("helped with ") || lower.startsWith("responsible for ")) {
            cleaned = "Delivered " + cleaned.substring(cleaned.indexOf(' ') + 1).trim();
        }
        cleaned = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        if (!cleaned.endsWith(".")) {
            cleaned += ".";
        }
        String cleanedLower = cleaned.toLowerCase();
        if (!jdKeywords.isEmpty() && jdKeywords.stream().noneMatch(k -> cleanedLower.contains(k.toLowerCase()))) {
            cleaned = cleaned.substring(0, cleaned.length() - 1) + " using " + jdKeywords.get(0) + ".";
        }
        return cleaned;
    }

    private List<String> extractTopKeywords(String text, int limit) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase());
        Map<String, Integer> frequency = new HashMap<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (STOP_WORDS.contains(token)) {
                continue;
            }
            frequency.put(token, frequency.getOrDefault(token, 0) + 1);
        }
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .limit(limit)
                .toList();
    }

    private String defaultObjective(ResumeDtos.ResumeRequest request) {
        String role = StringUtils.hasText(request.designation()) ? request.designation() : "software professional";
        String focus = StringUtils.hasText(request.jobDescription()) ? "aligned with the target role requirements" : "focused on measurable business impact";
        return "Motivated " + role + " seeking to apply technical expertise, problem-solving ability, and collaborative delivery skills in a role " + focus + ".";
    }

    private List<List<String>> projectInputs(ResumeDtos.ResumeRequest request) {
        return safe(request.projects()).stream().map(project -> safe(project.bullets())).toList();
    }

    private List<String> projectTitles(ResumeDtos.ResumeRequest request) {
        return safe(request.projects()).stream().map(project -> textOr(project.title(), "Project")).toList();
    }

    private List<List<String>> experienceInputs(ResumeDtos.ResumeRequest request) {
        return safe(request.experiences()).stream().map(experience -> safe(experience.bullets())).toList();
    }

    private List<String> experienceTitles(ResumeDtos.ResumeRequest request) {
        return safe(request.experiences()).stream()
                .map(experience -> textOr(experience.role(), "Experience"))
                .toList();
    }

    private List<List<String>> normalizeBulletGroups(List<List<String>> aiGroups, List<List<String>> inputs, List<String> titles) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            List<String> source = inputs.get(i);
            List<String> ai = aiGroups != null && aiGroups.size() > i && aiGroups.get(i) != null ? aiGroups.get(i) : List.of();
            List<String> normalized = new ArrayList<>();
            for (int j = 0; j < source.size(); j++) {
                String candidate = ai.size() > j ? ai.get(j) : null;
                normalized.add(StringUtils.hasText(candidate) ? candidate : enhanceFallbackBullet(source.get(j), titles.get(i)));
            }
            result.add(normalized);
        }
        return result;
    }

    private String enhanceFallbackBullet(String bullet, String title) {
        if (!StringUtils.hasText(bullet)) {
            return "Delivered meaningful contributions for " + title + " with attention to quality, clarity, and user needs.";
        }
        return Character.toUpperCase(bullet.trim().charAt(0)) + bullet.trim().substring(1);
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String textOr(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private boolean looksLikeUnresolvedPlaceholder(String value) {
        return value.startsWith("${") && value.endsWith("}");
    }
}

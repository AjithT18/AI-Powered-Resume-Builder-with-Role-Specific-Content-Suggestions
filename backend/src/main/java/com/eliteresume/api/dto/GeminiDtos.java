package com.eliteresume.api.dto;

import java.util.List;

public class GeminiDtos {
    public record GeminiEnhancement(
            String careerObjective,
            List<List<String>> projectBullets,
            List<List<String>> experienceBullets
    ) {}
}

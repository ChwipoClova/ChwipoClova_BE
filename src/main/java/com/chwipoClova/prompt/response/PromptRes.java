package com.chwipoClova.prompt.response;

import com.chwipoClova.prompt.entity.PromptCategory;
import lombok.Data;

import java.time.Instant;

@Data
public class PromptRes {

    private Long id;

    private String modelName;

    private String provider;

    private Integer maxTokens;

    private Integer maxOutputTokens;

    private String description;

    private Double temperature;

    private Double topP;

    private Integer topK;

    private Double frequencyPenalty;

    private Double presencePenalty;

    private Instant regDt;

    private Instant updDt;

    private String prompt;

    private String systemPrompt;

    private String promptName;

    private String useYn;

    private String url;

    private String key;

    private PromptCategory category;
}

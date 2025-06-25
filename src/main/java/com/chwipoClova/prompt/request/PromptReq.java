package com.chwipoClova.prompt.request;

import com.chwipoClova.prompt.entity.PromptCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PromptReq {

    private Long id;

    private String modelName;

    private String provider;

    private Integer maxTokens;

    private Integer maxOutputTokens;

    private String description;

    private BigDecimal temperature;

    private BigDecimal topP;

    private Integer topK;

    private BigDecimal frequencyPenalty;

    private BigDecimal presencePenalty;

    private Instant regDt;

    private Instant updDt;

    private String prompt;

    private String systemPrompt;

    private String promptName;

    private String useYn;

    private PromptCategory category;
}

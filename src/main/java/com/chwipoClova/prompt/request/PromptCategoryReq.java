package com.chwipoClova.prompt.request;

import lombok.Data;

import java.time.Instant;

@Data
public class PromptCategoryReq {
    private Long id;

    private String name;

    private Instant regDt;

    private Instant updDt;

    private String code;
}

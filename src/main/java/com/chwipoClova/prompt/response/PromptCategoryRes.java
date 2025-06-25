package com.chwipoClova.prompt.response;

import lombok.Data;

import java.time.Instant;

@Data
public class PromptCategoryRes {

    private Long id;

    private String name;

    private Instant regDt;

    private Instant updDt;

    private String code;
}

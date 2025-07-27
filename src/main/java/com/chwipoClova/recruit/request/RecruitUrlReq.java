package com.chwipoClova.recruit.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RecruitUrlReq {

    @Schema(description = "채용공고 url", example = "https://", name = "url")
    private String url;
}

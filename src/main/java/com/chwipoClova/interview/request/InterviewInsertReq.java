package com.chwipoClova.interview.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InterviewInsertReq {

    @Schema(description = "유저 ID", example = "1", name = "userId")
    private Long userId;

    @Schema(description = "이력서 ID", example = "1", name = "resumeId")
    private Long resumeId;

    @Schema(description = "채용공고내용", example = "삼성채용", name = "recruitContent")
    private String recruitContent;

    @Schema(description = "URL 여부", example = "false", name = "urlCk")
    private boolean urlCk;
}

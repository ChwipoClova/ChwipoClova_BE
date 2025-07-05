package com.chwipoClova.resume.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResumeUploadReq {

    @Schema(description = "유저 Id", example = "1", name = "userId")
    private Long userId;

    @Schema(description = "이력서 내용", example = "안녕하세요", name = "resumeContent")
    private String resumeContent;

}

package com.chwipoClova.recruit.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RecruitInsertReq {

    @Schema(description = "유저 ID", example = "1", name = "userId")
    private Long userId;

    @Schema(description = "채용공고 내용", example = "1", name = "recruitContent")
    private String recruitContent;

    private MultipartFile file;

    @Schema(description = "URL 여부", example = "false", name = "urlCk")
    private boolean urlCk;
}

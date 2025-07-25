package com.chwipoClova.resume.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ResumeListRes {

    @Schema(description = "이력서ID", example = "1", name = "resumeId")
    private Long resumeId;

    @Schema(description = "파일이름", example = "이력서.pdf", name = "fileName")
    private String fileName;

    @Schema(description = "등록일", example = "2023-12-09T10:13:17.838+00:00", name = "regDate")
    private Date regDate;

    @Schema(description = "이력서 내용", example = "이력서 내용", name = "content")
    private String content;
}

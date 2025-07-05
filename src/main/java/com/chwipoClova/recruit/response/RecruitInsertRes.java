package com.chwipoClova.recruit.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecruitInsertRes {

    @Schema(description = "채용공고 번호", example = "1", name = "recruitId")
    private Long recruitId;

    @Schema(description = "채용공고 제목", example = "삼성채용공고", name = "title")
    private String title;

    @Schema(description = "채용공고 요약", example = "삼성모집", name = "summary")
    private String summary;

    @Schema(description = "채용공고 원문", example = "삼성모집", name = "originText")
    private String originText;

}

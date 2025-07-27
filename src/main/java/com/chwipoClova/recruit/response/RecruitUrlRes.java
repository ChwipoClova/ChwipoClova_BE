package com.chwipoClova.recruit.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecruitUrlRes {
    @Schema(description = "회사명", example = "스톤즈랩", name = "name")
    @JsonProperty("company_name")
    private String name;

    @Schema(description = "직무명", example = "프론트엔드", name = "title")
    @JsonProperty("job_title")
    private String title;

    @Schema(description = "근무지역", example = "서울 강남구", name = "location")
    @JsonProperty("location")
    private String location;

    @Schema(description = "경력요구사항", example = "경년 3년 이상", name = "experience")
    @JsonProperty("experience")
    private String experience;
}

package com.chwipoClova.recruit.controller;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.recruit.request.RecruitUrlReq;
import com.chwipoClova.recruit.response.RecruitUrlRes;
import com.chwipoClova.recruit.service.RecruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Recruit", description = "이력서 API")
@RequestMapping("recruit")
public class RecruitController {

    private final RecruitService recruitService;

    @Operation(summary = "채용공고 삭제", description = "채용공고 삭제")
    @DeleteMapping(path = "/deleteRecruit")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))}
    )
    public CommonResponse deleteRecruit() {
        recruitService.deleteBeforeRecruit();
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    @Operation(summary = "채용공고 URL 요약", description = "채용공고 URL 요약")
    @PostMapping(path = "/getRecruitUrl")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))}
    )
    public List<RecruitUrlRes> getRecruitUrl(@RequestBody RecruitUrlReq recruitUrlReq) {
        return recruitService.getRecruitUrl(recruitUrlReq);
    }
}

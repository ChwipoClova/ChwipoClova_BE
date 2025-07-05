package com.chwipoClova.resume.controller;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.resume.request.ResumeDeleteOldReq;
import com.chwipoClova.resume.request.ResumeDeleteReq;
import com.chwipoClova.resume.request.ResumeUploadReq;
import com.chwipoClova.resume.response.ResumeListRes;
import com.chwipoClova.resume.response.ResumeUploadRes;
import com.chwipoClova.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Resume", description = "이력서 API")
@RequestMapping("resume")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "이력서 업로드", description = "이력서 업로드")
    @PostMapping(path = "/uploadResume", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    }
    )
    public ResumeUploadRes uploadResume(
            @Schema(description = "userId", example = "1", name = "userId")
            @RequestPart(value = "userId") Long userId,
            @RequestPart(value = "file") MultipartFile file
    ) throws Exception {
        return resumeService.uploadResume(userId, file, "");
    }

    @Operation(summary = "이력서 풀 업로드", description = "이력서 풀 업로드")
    @PostMapping(path = "/uploadResumeForText", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    }
    )
    public ResumeUploadRes uploadResumeForText(
            @RequestPart(value = "resumeData") ResumeUploadReq resumeData,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        return resumeService.uploadResume(resumeData.getUserId(), file, resumeData.getResumeContent());
    }

    @Operation(summary = "이력서 조회", description = "이력서 조회")
    @GetMapping(path = "/getResumeList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    }
    )
    public List<ResumeListRes> getResumeList(@Schema(description = "userId", example = "1", name = "userId") @RequestParam(name = "userId") Long userId) {
        return resumeService.selectResumeList(userId);
    }

    @Operation(summary = "이력서 삭제", description = "이력서 삭제")
    @DeleteMapping(path = "/deleteResume")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))}
    )
    public CommonResponse deleteResume(@RequestBody ResumeDeleteReq resumeDeleteReq) {
        return resumeService.deleteResume(resumeDeleteReq);
    }


    @Operation(summary = "오래된 이력서 삭제", description = "오래된 이력서 삭제")
    @DeleteMapping(path = "/deleteOldResume")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))}
    )
    public CommonResponse deleteOldResume(@RequestBody ResumeDeleteOldReq resumeDeleteOldReq) {
        return resumeService.deleteOldResume(resumeDeleteOldReq);
    }

}

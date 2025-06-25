package com.chwipoClova.tmp.controller;

import com.chwipoClova.tmp.entity.Tmp;
import com.chwipoClova.tmp.service.TmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Tmp", description = "Tmp API")
public class TmpController {

    private final TmpService tmpService;

    @Operation(summary = "Tmp 목록 조회 (기본 테스트)", description = "Tmp 목록 조회 (기본 테스트)")
    @GetMapping("/")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")}
    )
    public List<Tmp> getTmpList() throws Exception {
        return tmpService.selectTmpList();
    }
}

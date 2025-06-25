package com.chwipoClova.recruit.service;


import com.chwipoClova.api.service.ApiService;
import com.chwipoClova.common.enums.CommonCode;
import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.utils.FileUtil;
import com.chwipoClova.prompt.service.PromptService;
import com.chwipoClova.recruit.entity.Recruit;
import com.chwipoClova.recruit.entity.RecruitEditor;
import com.chwipoClova.recruit.repository.RecruitRepository;
import com.chwipoClova.recruit.request.RecruitInsertReq;
import com.chwipoClova.recruit.response.RecruitInsertRes;
import com.chwipoClova.user.entity.User;
import com.chwipoClova.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class RecruitService {

    @Value("${file.upload.recruit.path}")
    private String uploadPath;

    @Value("${file.upload.recruit.max-size}")
    private Long uploadMaxSize;

    @Value("${file.upload.recruit.type}")
    private String uploadType;

    private final UserRepository userRepository;

    private final RecruitRepository recruitRepository;

    //private final ApiUtils apiUtils;

    private final ApiService apiService;

    @Value("${api.url.base}")
    private String apiBaseUrl;

    @Value("${api.url.ocr}")
    private String ocr;

    @Value("${api.url.count}")
    private String count;

    @Value("${api.url.recruit}")
    private String recruit;

    @Value("${recruit.target.word1}")
    private String targetWord1;

    @Value("${recruit.target.word2}")
    private String targetWord2;

    @Value("${recruit.target.word3}")
    private String targetWord3;

    @Value("${recruit.target.word4}")
    private String targetWord4;

    private final PromptService promptService;

    @Transactional
    public RecruitInsertRes insertRecruit(RecruitInsertReq recruitInsertReq) throws Exception {
        Long userId = recruitInsertReq.getUserId();
        String recruitContent = recruitInsertReq.getRecruitContent();
        MultipartFile file = recruitInsertReq.getFile();

        User user = userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));

        if (org.apache.commons.lang3.StringUtils.isBlank(recruitContent) && file == null) {
            throw new CommonException(ExceptionCode.RECRUIT_CONTENT_NULL.getMessage(), ExceptionCode.RECRUIT_CONTENT_NULL.getCode());
        }

        Recruit recruit;
        int apiBaseTokenLimit = promptService.getMaxToken("RC");

        if (org.apache.commons.lang3.StringUtils.isNotBlank(recruitContent)) {

            // 토큰 수 계산
            apiService.countTokenLimitCk(recruitContent, apiBaseTokenLimit);

            // 채용공고 요약 실행
            String summary = apiService.summaryRecruit(recruitContent);

            // 채용공고에서 제목 추출
            String title = getRecruitTitle(summary);

            recruit = Recruit.builder()
                    .title(title)
                    .content(recruitContent)
                    .summary(summary)
                    .user(user)
                    .build();
        } else {

            String contentType = FileUtil.getOriginalFileExtension(file);

            if (org.apache.commons.lang3.StringUtils.isBlank(contentType) || !contentType.toLowerCase().contains(uploadType)) {
                throw new CommonException(ExceptionCode.FILE_EXT_IMAGE.getMessage(), ExceptionCode.FILE_EXT_IMAGE.getCode());
            }

            String originalName = file.getOriginalFilename();
            assert originalName != null;

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

            // 날짜 폴더 생성
            String folderPath = makeFolder();

            // UUID
            String uuid = UUID.randomUUID().toString();

            long currentTimeMills = Timestamp.valueOf(LocalDateTime.now()).getTime();

            String filePath = uploadPath + File.separator + folderPath + File.separator;
            String fileName = uuid + "_" + currentTimeMills + "." +extension;
            Long fileSize = file.getSize();

            if (fileSize > uploadMaxSize) {
                throw new CommonException(ExceptionCode.FILE_SIZE.getMessage(), ExceptionCode.FILE_SIZE.getCode());
            }

            // 저장할 파일 이름 중간에 "_"를 이용해서 구현
            String saveName = filePath + fileName;
            Path savePath = Paths.get(saveName);
            file.transferTo(savePath);

            // 채용공고 OCR
            String resumeTxt = apiService.ocr(savePath.toFile());

            // 채용공고 OCR 성공 이후 토큰 계산 하여 3000자 이하인자 체크
            apiService.countTokenLimitCk(resumeTxt, apiBaseTokenLimit);

            // 채용공고 요약
            String summary = apiService.summaryRecruit(resumeTxt);

            // 채용공고에서 제목 추출
            String title = getRecruitTitle(summary);

            recruit = Recruit.builder()
                    .title(title)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileSize(fileSize)
                    .originalFileName(originalName)
                    .user(user)
                    .summary(summary)
                    .build();

            // 파일 삭제
/*            try {
                Files.delete(savePath);
            } catch (IOException e) {
                log.error("Failed to delete file: {}", savePath, e);
                throw new CommonException("File deletion failed", ExceptionCode.SERVER_ERROR.getCode());
            }*/
        }

        Recruit recruitRst = recruitRepository.save(recruit);

        return RecruitInsertRes.builder()
                .recruitId(recruitRst.getRecruitId())
                .title(recruitRst.getTitle())
                .summary(recruitRst.getSummary())
                .build();
    }

    private String getRecruitTitle(String summary) {
        String title = null;
        // 현재 사용하지 않는 --- 제거 및 줄바꿈 분리
            String[] splitSummaryList = summary.split("\n");

            // 기업명 고정
            for (String splitSummary : splitSummaryList) {
                if (splitSummary.indexOf(".") != -1) {
                    String num = splitSummary.substring(0, splitSummary.indexOf("."));
                    if (org.apache.commons.lang3.StringUtils.isNumeric(num)) {
                        int index1 = splitSummary.indexOf(targetWord1);
                        int index2 = splitSummary.indexOf(targetWord2);
                        int index3 = splitSummary.indexOf(targetWord3);
                        int index4 = splitSummary.indexOf(targetWord4);

                        if (index1 != -1) {
                            index1 = splitSummary.indexOf(targetWord1);
                            if (index1 != -1) {
                                title = splitSummary.substring(index1 + targetWord1.length()).trim();
                            }
                        } else if (index2 != -1) {
                            index2 = splitSummary.indexOf(targetWord2);
                            if (index2 != -1) {
                                title = splitSummary.substring(index2 + targetWord2.length()).trim();
                            }
                        } else if (index3 != -1) {
                            index3 = splitSummary.indexOf(targetWord3);
                            if (index3 != -1) {
                                title = splitSummary.substring(index3 + targetWord3.length()).trim();
                            }
                        } else if (index4 != -1) {
                            index4 = splitSummary.indexOf(targetWord4);
                            if (index4 != -1) {
                                title = splitSummary.substring(index4 + targetWord4.length()).trim();
                            }
                        }
                    }
                }
            }

        if (org.apache.commons.lang3.StringUtils.isBlank(title)) {
            throw new CommonException(ExceptionCode.RECRUIT_TITLE_NULL.getMessage(), ExceptionCode.RECRUIT_TITLE_NULL.getCode());
        }
        return title;
    }

    private String makeFolder() {

        String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String folderPath = str.replace("/", File.separator);

        // make folder --------
        File uploadPathFolder = new File(uploadPath, folderPath);

        if(!uploadPathFolder.exists()) {
            boolean mkdirs = uploadPathFolder.mkdirs();
            log.info("-------------------makeFolder------------------");
            log.info("uploadPathFolder.exists() : {}", uploadPathFolder.exists());
            log.info("mkdirs : {}", mkdirs);
        }
        return folderPath;
    }

    @Transactional
    public void deleteBeforeRecruit() {
        Timestamp baseDate = Timestamp.valueOf(LocalDate.now().minusDays(14).atStartOfDay());
        List<Recruit> recruitList = recruitRepository.findByDelFlagAndRegDateLessThanEqual(CommonCode.DELETE_N.getCode(),baseDate);
        if (recruitList != null) {
            if (recruitList.size() > 0) {
                log.info("deleteBeforeRecruit size {}, baseDate {}", recruitList.size() , baseDate);

                // 채용공고 삭제에서 상태값 변경으로 수정
                // recruitRepository.deleteAll(recruitList);
                recruitList.stream().forEach(recruit -> {
                    RecruitEditor.RecruitEditorBuilder editorBuilder = recruit.toEditor();
                    RecruitEditor recruitEditor = editorBuilder.delFlag(CommonCode.DELETE_Y.getCode())
                            .build();
                    recruit.edit(recruitEditor);
                });
            }
        }
    }

}

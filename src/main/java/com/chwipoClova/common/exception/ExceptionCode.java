package com.chwipoClova.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@AllArgsConstructor
public enum ExceptionCode {

    BAD_REQUEST(String.valueOf(HttpStatus.BAD_REQUEST.value()), "잘못된 요청입니다."),

    NOT_FOUND(String.valueOf(HttpStatus.NOT_FOUND.value()), "요청한 페이지를 찾을 수 없습니다."),

    SERVER_ERROR(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "내부 서버 오류입니다."),



    // Custom Exception
    SECURITY("800", "로그인이 필요합니다"),

    USER_NULL("801", "유저 정보가 올바르지 않습니다."),

    FILE_EXT_PDF("850", "PDF 파일 형식이 아닙니다."),

    FILE_EXT_IMAGE("851", "IMAGE 파일 형식이 아닙니다."),

    FILE_SIZE("852", "파일 업로드 최대 크기는 50M 입니다."),

    FILE_PDF_PAGE_OVER("853", "PDF 최대 장수를 넘었습니다."),

    RESUME_NULL("860", "이력서 정보가 올바르지 않습니다."),

    RESUME_LIST_OVER("861", "이력서 최대 개수를 초과하였습니다."),

    RESUME_TOKEN_OVER("862", "이력서의 글자 수를 3,000자 이하로 줄여주세요"),

    RESUME_NOT_DELETE("863", "삭제할 수 없는 이력서 입니다."),

    RECRUIT_CONTENT_NULL("870", "채용공고 정보가 올바르지 않습니다."),

    RECRUIT_TITLE_NULL("871", "채용공고 기업명이 없습니다."),

    RECRUIT_URL_NULL("872", "채용공고 URL이 없습니다.."),

    INTERVIEW_NULL("880", "면접 정보가 올바르지 않습니다."),

    INTERVIEW_LIST_OVER("881", "면접 최대 개수를 초과하였습니다."),

    INTERVIEW_COMPLETE("882", "완료 된 면접 입니다."),

    INTERVIEW_NOT_COMPLETE("883", "미완료 된 면접 입니다."),

    QA_NULL("890", "질문 정보가 올바르지 않습니다."),

    FEEDBACK_NOT_NULL("891", "피드백 데이터가 있습니다."),

    TOKEN_NULL("401", "권한이 없습니다."),

    INVALID_TOKEN("951", "유효하지 않은 토큰입니다."),

    API_NULL("980", "API 결과 NULL"),

    API_JSON_MAPPING_FAIL("981", "API JSON 매핑에 실패했습니다."),

    API_NOT_OK("982", "API 성공 응답이 아닙니다."),

    API_RESUME_FAIL("983", "이력서 API 호출 실패"),

    API_RESUME_SUMMARY_NULL("984", "이력서 요약 결과가 비어있습니다."),

    API_TOKEN_COUNT_FAIL("985", "토큰 계산기 호출 실패"),

    API_QA_NULL("986", "질문 API 결과가 비어있습니다."),

    API_TOKEN_COUNT_OVER("987", "글자수를 5000자 이하로 줄여주세요."),

    CATEGORY_NULL("701", "카테고리 정보가 올바르지 않습니다."),

    SUBSCRIPTION_NULL("601", "구독정보가 올바르지 않습니다.")
    ;

    private static final ExceptionCode[] VALUES;

    static {
        VALUES = values();
    }

    private final String code;
    private final String message;

    public static ExceptionCode resolve(String statusCode) {
        for (ExceptionCode status : VALUES) {
            if (status.code.equals(statusCode)) {
                return status;
            }
        }
        return null;
    }
}

package com.chwipoClova.api.service;

import com.chwipoClova.resume.response.ApiRes;
import org.springframework.http.HttpEntity;

import java.io.File;
import java.net.URI;

public interface ApiService {

    String callApi(URI apiUrl, String reqData, HttpEntity<?> entity);

    String ocr(File file) throws Exception;

    int countToken(String summary);

    boolean countTokenLimitCk(String text, int limitCnt);

    String summaryResume(String resumeTxt);

    String summaryRecruit(String recruitTxt);

    String question(String recruitSummary, String resumeSummary);

    String feel(String allQa);

    String keyword(String qa);

    String best(String question, String answer);

    ApiRes callApiForJson(URI apiUrl, String reqData, HttpEntity<?> entity);

    String summaryRecruitUrl(String url);
}

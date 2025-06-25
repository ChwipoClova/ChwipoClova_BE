package com.chwipoClova.api.service;

import com.chwipoClova.common.dto.UserDetailsImpl;
import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.repository.LogRepository;
import com.chwipoClova.resume.response.ApiRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
//public class ApiNaverServiceImpl implements ApiService {
public class ApiNaverServiceImpl {

    private final RestTemplate restTemplate;

    private final LogRepository logRepository;

    @Value("${api.url.base}")
    private String apiBaseUrl;

    @Value("${api.url.ocr}")
    private String ocr;

    @Value("${api.url.count}")
    private String count;

    @Value("${api.url.resume}")
    private String resume;

    @Value("${api.url.recruit}")
    private String recruit;

    @Value("${api.url.question}")
    private String question;

    @Value("${api.url.feel}")
    private String feel;

    @Value("${api.url.keyword}")
    private String keyword;

    @Value("${api.url.best}")
    private String best;

    public String callApi(URI apiUrl, String reqData, HttpEntity<?> entity) {
        String resultData = null;
        String resultMessage;
        Long userId = null;
        ResponseEntity<String> responseAsString = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            userId = ((UserDetailsImpl) authentication.getPrincipal()).getUser().getUserId();
        }
        try {
            responseAsString = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            log.info("responseAsString : " +  responseAsString);
            if (responseAsString == null) {
                resultMessage = "API 결과 NULL";
                log.info("API 결과 NULL");
            } else {
                if (responseAsString.getStatusCode() == HttpStatus.OK) {
                    resultMessage = "API 성공";
                    log.info("API 성공");
                    resultData = responseAsString.getBody();
                } else {
                    resultMessage = "API 통신 결과 실패 HttpStatus" + responseAsString.getStatusCode();
                    log.error("API 통신 결과 실패 HttpStatus : {} ", responseAsString.getStatusCode());
                }
            }
        } catch (Exception e) {
            resultMessage = "callApi 실패 error " + e.getMessage();
            log.error("callApi 실패 error : {}", e.getMessage());
        }

        if (resultData == null) {
            resultMessage = ExceptionCode.API_NULL.getMessage();
        }

        // API 로그 적재
        logRepository.apiLogSave(userId, apiUrl.toString(), reqData, responseAsString.toString(), resultMessage);

        if (resultData == null) {
            throw new CommonException(ExceptionCode.API_NULL.getMessage(), ExceptionCode.API_NULL.getCode());
        }

        return resultData;
    }

    public String ocr(MultipartFile file) throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename(){
                return file.getOriginalFilename();
            }
        };
        body.add("file", contentsAsResource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + ocr)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);

        String reqData = "file : " + file.getOriginalFilename();

        return callApi(apiUrl, reqData, requestEntity);
    }

    public String countToken(String summary) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> requestEntity = new HttpEntity<>(summary, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + count)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("summary : " +  summary);

        String reqData = "summary : " + summary;

        String count = callApi(apiUrl, reqData, requestEntity);

        if (!org.apache.commons.lang3.StringUtils.isNumeric(count)) {
            new CommonException(ExceptionCode.API_TOKEN_COUNT_FAIL.getMessage(), ExceptionCode.API_TOKEN_COUNT_FAIL.getCode());
        }

        return count;
    }

    public boolean countTokenLimitCk(String text, int limitCnt) {
        String count = countToken(text);
        int tokenCnt = Integer.parseInt(count);
        if (tokenCnt >= limitCnt) {
            throw new CommonException(ExceptionCode.API_TOKEN_COUNT_OVER.getMessage(), ExceptionCode.API_TOKEN_COUNT_OVER.getCode());
        } else {
            return true;
        }
    }

    public String summaryResume(String resumeTxt) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> requestEntity = new HttpEntity<>(resumeTxt, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + resume)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("resumeTxt : " +  resumeTxt);

        String reqData = "resumeTxt : " + resumeTxt;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public String summaryRecruit(String recruitTxt) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> requestEntity = new HttpEntity<>(recruitTxt, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + recruit)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("recruitTxt : " +  recruitTxt);

        String reqData = "recruitTxt : " + recruitTxt;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public String question(String recruitSummary, String resumeSummary) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("recruit_summary", recruitSummary);
        body.add("resume_summary", resumeSummary);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + question)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("recruitSummary : " +  recruitSummary);
        log.info("resumeSummary : " +  resumeSummary);

        String reqData = "recruitSummary : " + recruitSummary + "######" + " resumeSummary : " + resumeSummary;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public String feel(String allQa) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> requestEntity = new HttpEntity<>(allQa, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + feel)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("feel : " + allQa);

        String reqData = "feel : " + allQa;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public String keyword(String qa) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> requestEntity = new HttpEntity<>(qa, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + keyword)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("keyword : " + qa);

        String reqData = "keyword : " + qa;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public String best(String question, String answer) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("question", question);
        body.add("answer", answer);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        URI apiUrl = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + best)
                .build(true)
                .toUri();
        log.info("uri : " +  apiUrl);
        log.info("question : " + question);
        log.info("answer : " + answer);

        String reqData = "question : " + question + "######" + " answer : " + answer;

        ApiRes response = callApiForJson(apiUrl, reqData, requestEntity);
        return response.getResult().getMessage().getContent();
    }

    public ApiRes callApiForJson(URI apiUrl, String reqData, HttpEntity<?> entity) {
        return josnConvertToVo(callApi(apiUrl, reqData, entity));
    }

    private <T> T xmlConvertToVo(String xml, Class<T> voClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(voClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        StringReader reader = new StringReader(xml);
        return (T)unmarshaller.unmarshal(reader);
    }

    private ApiRes josnConvertToVo(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);;
            ApiRes response = objectMapper.readValue(json, ApiRes.class);

            if (response == null)  {
                throw new CommonException(ExceptionCode.API_JSON_MAPPING_FAIL.getMessage(), ExceptionCode.API_JSON_MAPPING_FAIL.getCode());
            }

            if (!org.apache.commons.lang3.StringUtils.equals(response.getStatus().getCode(), "20000")) {
                throw new CommonException(ExceptionCode.API_NOT_OK.getMessage(), ExceptionCode.API_NOT_OK.getCode());
            }

            return response;
        } catch (JsonProcessingException e) {
            log.error("josnConvertToVo error {}", e.getMessage());
            throw new CommonException(ExceptionCode.API_JSON_MAPPING_FAIL.getMessage(), ExceptionCode.API_JSON_MAPPING_FAIL.getCode());
        }
    }
}

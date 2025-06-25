package com.chwipoClova.api.service;

import com.chwipoClova.api.deepseek.res.DeepSeekRes;
import com.chwipoClova.api.deepseek.service.DeepSeekService;
import com.chwipoClova.common.dto.UserDetailsImpl;
import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.repository.LogRepository;
import com.chwipoClova.common.utils.FileUtil;
import com.chwipoClova.prompt.response.PromptRes;
import com.chwipoClova.prompt.service.PromptService;
import com.chwipoClova.resume.response.ApiRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiGoogleServiceImpl implements ApiService {

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

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.ocr.bucket-name}")
    private String bucketName;

    private final PromptService promptService;

    private final DeepSeekService deepseekService;

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

    public String callApi(PromptRes promptRes) {
        DeepSeekRes deepSeekRes = deepseekService.askDeepSeek(promptRes);

        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            userId = userDetails.getUser().getUserId();
        }
        // API 로그 적재
        logRepository.apiLogSave(userId, promptRes.getUrl(), promptRes.getSystemPrompt() + promptRes.getPrompt(), deepSeekRes.getResult(), String.valueOf(deepSeekRes.getStatus()));

        if (deepSeekRes.getStatus() != HttpStatus.OK.value()) {
            throw new CommonException(ExceptionCode.API_NULL.getMessage(), ExceptionCode.API_NULL.getCode());
        }

        return deepSeekRes.getResult();
    }

    public String ocr(File file) throws Exception {
        // pdf, img 파일 구분
        String extension = FileUtil.getOriginalFileExtension(file);

        if (extension != null && extension.equalsIgnoreCase("pdf")) {
            // 1. GCS에 업로드
            String objectName = "tikitaka/" + "resume/" + UUID.randomUUID() + ".pdf";
            Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
            BlobId blodId = uploadToGCS(storage, bucketName, objectName, file);

            // 2. OCR 요청
            String gcsInputUri = "gs://" + bucketName + "/" + objectName;
            String gcsOutputUri = "gs://" + bucketName + "/tikitaka" + "/resume" + "/output/" + UUID.randomUUID() + "/";
            String resultGcsUri = asyncBatchAnnotatePDF(gcsInputUri, gcsOutputUri);

            // 3. GCS에서 데이터 읽기
            String resultBucketName = extractBucketName(resultGcsUri);
            String prefix = extractPrefix(resultGcsUri);
            String resultText = extractTextFromOcrOutput(resultBucketName, prefix);

            // 4. GCS에서 임시 파일 삭제
            deleteToGCS(storage, blodId, resultBucketName, prefix);

            return resultText;
        } else {
            return ocrImageFile(file);
        }
    }

    private String ocrImageFile(File file) throws IOException {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.readFrom(new FileInputStream(file));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                log.error("OCR 에러: " + res.getError().getMessage());
                throw new IOException("이미지 읽기를 실패하였습니다.");
            }

            return res.getFullTextAnnotation().getText();
        }
    }

    private void deleteToGCS(Storage storage, BlobId blodId, String bucketName, String gcsOutputUri) {
        // 7. GCS에 업로드된 임시 파일 및 결과 파일 삭제 (매우 중요!)
        log.info("Cleaning up temporary GCS files...");
        // 입력 PDF 파일 삭제
        Blob inputBlobToDelete = storage.get(blodId);
        if (inputBlobToDelete != null) {
            inputBlobToDelete.delete();
        }

        // 출력 JSON 파일들 삭제
        Page<Blob> outputBlobsToDelete = storage.list(bucketName, Storage.BlobListOption.prefix(gcsOutputUri));
        for (Blob blob : outputBlobsToDelete.iterateAll()) {
            blob.delete();
        }
        log.info("Temporary GCS files cleaned up.");
    }

    private BlobId uploadToGCS(Storage storage, String bucketName, String objectName, File file) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/pdf").build();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return blobId;
    }

    private String asyncBatchAnnotatePDF(String gcsSourceUri, String gcsDestinationUri) throws Exception {
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            GcsSource gcsSource = GcsSource.newBuilder().setUri(gcsSourceUri).build();
            InputConfig inputConfig = InputConfig.newBuilder()
                    .setMimeType("application/pdf")
                    .setGcsSource(gcsSource)
                    .build();

            GcsDestination gcsDestination = GcsDestination.newBuilder().setUri(gcsDestinationUri).build();
            OutputConfig outputConfig = OutputConfig.newBuilder()
                    .setGcsDestination(gcsDestination)
                    .setBatchSize(1)
                    .build();

            AsyncAnnotateFileRequest request = AsyncAnnotateFileRequest.newBuilder()
                    .setInputConfig(inputConfig)
                    .addFeatures(Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION))
                    .setOutputConfig(outputConfig)
                    .build();

            OperationFuture<AsyncBatchAnnotateFilesResponse, OperationMetadata> future =
                    client.asyncBatchAnnotateFilesAsync(List.of(request));

            AsyncBatchAnnotateFilesResponse response = future.get(300, TimeUnit.SECONDS);

            if (response.getResponsesCount() == 0 || !response.getResponses(0).hasOutputConfig()) {
                log.error("OCR 결과가 없습니다. 입력 파일을 확인하세요." + "gcsSourceUri : " +gcsSourceUri + "gcsDestinationUri : " +gcsDestinationUri);
                throw new IOException("결과가 없습니다. 입력 파일을 확인하세요.");
            }
            // 결과 위치 확인
            return response.getResponses(0).getOutputConfig().getGcsDestination().getUri();
        }
    }

    private String extractBucketName(String gcsUri) {
        // "gs://your-bucket/output/folder/" → "your-bucket"
        return gcsUri.replace("gs://", "").split("/", 2)[0];
    }

    private String extractPrefix(String gcsUri) {
        // "gs://your-bucket/output/folder/" → "output/folder/"
        return gcsUri.replace("gs://", "").split("/", 2)[1];
    }

    private String extractTextFromOcrOutput(String bucketName, String outputPrefix) throws Exception {
        StringBuilder resultText = new StringBuilder();

        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Vision OCR 결과 JSON들이 저장된 GCS 경로 확인
        com.google.api.gax.paging.Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(outputPrefix));

        for (Blob blob : blobs.iterateAll()) {
            if (!blob.getName().endsWith(".json")) continue;

            // JSON 파일 읽기
            String json = new String(blob.getContent());

            // JSON 파싱 → AnnotateFileResponse 객체 생성
            AnnotateFileResponse.Builder responseBuilder = AnnotateFileResponse.newBuilder();
            JsonFormat.parser().merge(json, responseBuilder);
            AnnotateFileResponse response = responseBuilder.build();

            // OCR 텍스트 추출
            for (AnnotateImageResponse imageResponse : response.getResponsesList()) {
                if (imageResponse.hasFullTextAnnotation()) {
                    resultText.append(imageResponse.getFullTextAnnotation().getText());
                }
            }
        }
        return resultText.toString().trim();
    }

    public int countToken(String summary) {
        if (summary == null || summary.isBlank()) return 0;

        int ascii = (int) summary.chars().filter(c -> c < 128).count();
        double asciiRatio = (double) ascii / summary.length();

        // 영어: 약 0.75, 한글 혼합: 약 1.1
        return asciiRatio > 0.85
                ? (int)(summary.length() * 0.75)
                : (int)(summary.length() * 1.1);
    }

    public boolean countTokenLimitCk(String text, int limitCnt) {
        int tokenCnt = countToken(text);
        if (tokenCnt >= limitCnt) {
            throw new CommonException(ExceptionCode.API_TOKEN_COUNT_OVER.getMessage(), ExceptionCode.API_TOKEN_COUNT_OVER.getCode());
        } else {
            return true;
        }
    }

    public String summaryResume(String resumeTxt) {
        PromptRes promptRes = promptService.getPrompt("RS");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${OCR된 이력서}$", resumeTxt);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
    }

    public String summaryRecruit(String recruitTxt) {
        PromptRes promptRes = promptService.getPrompt("RC");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${사용자가 입력/OCR 된 채용공고}$", recruitTxt);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
    }

    public String question(String recruitSummary, String resumeSummary) {
        PromptRes promptRes = promptService.getPrompt("QA");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${이력서 요약 프롬프트 출력값}$", resumeSummary);
        prompt = prompt.replace("${채용공고 요약 프롬프트 출력값}$", recruitSummary);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
    }

    public String feel(String allQa) {
        PromptRes promptRes = promptService.getPrompt("SC");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${면접 시뮬레이션에서 질문마다 사용자가 답변한 답변 리스트}$", allQa);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
    }

    public String keyword(String qa) {
        PromptRes promptRes = promptService.getPrompt("KY");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${면접 시뮬레이션에서 질문마다 사용자가 답변한 답변 리스트}$", qa);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
    }

    public String best(String question, String answer) {
        PromptRes promptRes = promptService.getPrompt("BT");
        String prompt = promptRes.getPrompt();
        prompt = prompt.replace("${면접 시뮬레이션에서 활용된 질문 리스트}$", question);
        prompt = prompt.replace("${면접 시뮬레이션에서 질문마다 사용자가 답변한 답변 리스트}$", answer);
        promptRes.setPrompt(prompt);
        return callApi(promptRes);
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

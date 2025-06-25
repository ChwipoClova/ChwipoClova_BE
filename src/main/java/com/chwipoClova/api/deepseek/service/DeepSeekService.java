package com.chwipoClova.api.deepseek.service;

import com.chwipoClova.api.deepseek.req.DeepSeekRequest;
import com.chwipoClova.api.deepseek.req.Message;
import com.chwipoClova.api.deepseek.res.DeepSeekApiResponse;
import com.chwipoClova.api.deepseek.res.DeepSeekRes;
import com.chwipoClova.prompt.response.PromptRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeepSeekService {

    private final RestTemplate restTemplate;

    public DeepSeekRes askDeepSeek(PromptRes promptRes) {
        DeepSeekRes deepSeekRes = new DeepSeekRes();
        List<Message> messages = List.of(
                new Message("system", promptRes.getSystemPrompt()),
                new Message("user", promptRes.getPrompt())
        );

        DeepSeekRequest request = new DeepSeekRequest(
                promptRes.getModelName(),
                messages,
                promptRes.getMaxOutputTokens(),
                promptRes.getTemperature(),
                promptRes.getTopP(),
                false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(promptRes.getKey());

        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<DeepSeekApiResponse> response = restTemplate.exchange(
                promptRes.getUrl(),
                HttpMethod.POST,
                entity,
                DeepSeekApiResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            deepSeekRes.setResult(response.getBody().getChoices().get(0).getMessage().getContent());
        } else {
            log.error("DeepSeek API 요청 실패: {}", response.getStatusCode());
        }
        deepSeekRes.setStatus(response.getStatusCode().value());
        return deepSeekRes;
    }
}

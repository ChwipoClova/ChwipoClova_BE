package com.chwipoClova.prompt.service;

import com.chwipoClova.prompt.entity.Prompt;
import com.chwipoClova.prompt.entity.PromptCategory;
import com.chwipoClova.prompt.repository.PromptCategoryRepository;
import com.chwipoClova.prompt.repository.PromptRepository;
import com.chwipoClova.prompt.response.PromptCategoryRes;
import com.chwipoClova.prompt.response.PromptRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptDeepSeekServiceImpl implements PromptService {

    private final PromptCategoryRepository promptCategoryRepository;
    private final PromptRepository promptRepository;

    @Override
    public PromptCategoryRes getPromptCategory(String code) {
        Optional<PromptCategory> promptCategoryRes = promptCategoryRepository.findByCodeIgnoreCase(code);
        PromptCategoryRes response = new PromptCategoryRes();
        if (promptCategoryRes.isPresent()) {
            PromptCategory category = promptCategoryRes.get();
            log.info("Prompt category found: {}", category);
            response.setId(category.getId());
            response.setName(category.getName());
            response.setRegDt(category.getRegDt());
            response.setUpdDt(category.getUpdDt());
            response.setCode(category.getCode());
        } else {
            log.warn("Prompt category with code '{}' not found", code);
        }
        return response;
    }

    @Override
    public PromptRes getPrompt(String code) {
        List<Prompt> promptList = promptRepository.findByCategory_CodeIgnoreCaseAndUseYnIgnoreCaseOrderByUpdDtDesc(code, "Y");
        PromptRes response = new PromptRes();
        if (!promptList.isEmpty()) {
            Prompt prompt = promptList.get(0);
            response = convertToPromptRes(prompt);
        } else {
            log.warn("No prompts found for category ID '{}'", code);
        }
        return response;
    }

    @Override
    public int getMaxToken(String code) {
        List<Prompt> promptList = promptRepository.findByCategory_CodeIgnoreCaseAndUseYnIgnoreCaseOrderByCategory_UpdDtDesc(code, "Y");
        if (!promptList.isEmpty()) {
            Prompt prompt = promptList.get(0);
            PromptRes response = convertToPromptRes(prompt);
            log.info("Max tokens for code '{}': {} : {}", code, response.getMaxTokens(), response.getMaxOutputTokens());
            return response.getMaxTokens() - response.getMaxOutputTokens();
        }
        return 0;
    }

    private PromptRes convertToPromptRes(Prompt prompt) {
        PromptRes response = new PromptRes();
        response.setId(prompt.getId());
        response.setPrompt(prompt.getPrompt());
        response.setPromptName(prompt.getPromptName());
        response.setSystemPrompt(prompt.getSystemPrompt());
        response.setDescription(prompt.getDescription());
        response.setFrequencyPenalty(prompt.getFrequencyPenalty());
        response.setCategory(prompt.getCategory());
        response.setMaxTokens(prompt.getMaxTokens());
        response.setMaxOutputTokens(prompt.getMaxOutputTokens());
        response.setModelName(prompt.getModelName());
        response.setProvider(prompt.getProvider());
        response.setTemperature(prompt.getTemperature());
        response.setTopP(prompt.getTopP());
        response.setTopK(prompt.getTopK());
        response.setUseYn(prompt.getUseYn());
        response.setUrl(prompt.getUrl());
        response.setKey(prompt.getKey());
        response.setRegDt(prompt.getRegDt());
        response.setUpdDt(prompt.getUpdDt());
        return response;
    }
}

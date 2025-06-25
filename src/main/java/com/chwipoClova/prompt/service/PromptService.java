package com.chwipoClova.prompt.service;

import com.chwipoClova.prompt.response.PromptCategoryRes;
import com.chwipoClova.prompt.response.PromptRes;

public interface PromptService {

    PromptCategoryRes getPromptCategory(String code);

    PromptRes getPrompt(String code);

    int getMaxToken(String code);

}

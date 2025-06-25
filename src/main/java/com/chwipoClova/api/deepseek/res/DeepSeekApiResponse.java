package com.chwipoClova.api.deepseek.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekApiResponse {
    private String id;
    private String object;
    private List<ChatChoice> choices;
    private Usage usage;
}

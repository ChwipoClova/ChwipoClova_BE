package com.chwipoClova.api.deepseek.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekRequest {
    private String model;
    private List<Message> messages;
    private Integer max_tokens;
    private Double temperature;
    private Double top_p;
    private Boolean stream;
}

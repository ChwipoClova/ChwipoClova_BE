package com.chwipoClova.api.deepseek.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String role;     // "system", "user", "assistant"
    private String content;
}

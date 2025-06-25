package com.chwipoClova.api.deepseek.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatChoice {
    private int index;
    private ChatMessage message;
    private String finish_reason;
}

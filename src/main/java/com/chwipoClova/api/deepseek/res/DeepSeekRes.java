package com.chwipoClova.api.deepseek.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekRes {
    private String result;
    private int status;
}

package com.chwipoClova.token.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class TokenReq {
    @Schema(description = "유저ID", example = "1", name = "userId")
    private String userId;

    @JsonCreator
    public TokenReq(@JsonProperty("userId") String userId) {
        this.userId = userId;
    }
}

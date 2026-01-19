package com.chwipoClova.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public enum UserLoginType {
    KAKAO("kakao",1, "카카오"),
    GOOGLE("google", 2, "구글"),
    APPLE("apple", 3, "애플")
    ;

    private final String registrationId;
    private final Integer code;
    private final String name;

    private static final UserLoginType[] VALUES;

    static {
        VALUES = values();
    }

    public static String getLoginTypeName(Integer code) {
        for (UserLoginType status : VALUES) {
            if (Objects.equals(status.code, code)) {
                return status.getName();
            }
        }
        return "";
    }
}

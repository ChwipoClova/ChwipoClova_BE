package com.chwipoClova.oauth2.dto;

import com.chwipoClova.login.dto.GoogleLoginDto;
import com.chwipoClova.oauth2.enums.UserLoginType;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class GoogleOAuth2UserInfo implements UserInfo {

    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickName;
    private final String profileImageUrl;

    public GoogleOAuth2UserInfo(String accessToken, Map<String, Object> attributes) {
        this.accessToken = accessToken;
        this.attributes = attributes;
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.firstName = (String) attributes.get("given_name");
        this.lastName = (String) attributes.get("family_name");
        this.nickName = (String) attributes.get("name");
        this.profileImageUrl = (String) attributes.get("picture");
    }
    public GoogleOAuth2UserInfo(GoogleLoginDto googleLoginDto) {
        this.accessToken = "";
        this.attributes = Map.of();
        this.firstName = "";
        this.lastName = "";
        this.id = googleLoginDto.getSub();
        this.email = googleLoginDto.getEmail();
        this.name = googleLoginDto.getName();
        this.nickName = googleLoginDto.getName();
        this.profileImageUrl = googleLoginDto.getPicture();
    }

    @Override
    public UserLoginType getOAuthProvider() {
        return UserLoginType.GOOGLE;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFirstName() {
        return isNullOrBlank(firstName) ? "" : firstName;
    }

    @Override
    public String getLastName() {
        return  isNullOrBlank(lastName) ? "" : lastName;
    }

    public boolean isNullOrBlank(String str) {
        return StringUtils.isBlank(str) || "null".equalsIgnoreCase(str);
    }

    @Override
    public String getNickname() {
        String fullName = getLastName() + getFirstName();
        if (StringUtils.isBlank(fullName)) {
            return nickName;
        } else {
            return fullName;
        }
    }

    @Override
    public String getThumbnailImageUrl() {
        return profileImageUrl;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
package com.atahani.telepathy.model;

/**
 * Sign in request body model
 */
public class SignInRequestModel {
    private String app_id;
    private String app_key;
    private String provider;
    private String access_token;
    private String device_model;

    public SignInRequestModel() {
    }

    public SignInRequestModel(String appId, String appKey, String provider, String accessToken, String deviceModel) {
        this.app_id = appId;
        this.app_key = appKey;
        this.provider = provider;
        this.access_token = accessToken;
        this.device_model = deviceModel;
    }
}

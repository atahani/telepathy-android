package com.atahani.telepathy.model;

/**
 * Register request device request model
 */
public class RegisterDeviceRequestModel {
    private String token_type;
    private String token;
    private String device_model;
    private String os_version;
    private String app_version;

    public RegisterDeviceRequestModel() {
    }

    public RegisterDeviceRequestModel(String tokenType, String token, String deviceModel, String osVersion, String appVersion) {
        this.token_type = tokenType;
        this.token = token;
        this.device_model = deviceModel;
        this.os_version = osVersion;
        this.app_version = appVersion;
    }

}

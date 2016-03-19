package com.atahani.telepathy.model;

import com.atahani.telepathy.network.ClientConfigs;

/**
 * Refresh token request model
 */
public class RefreshTokenRequestModel {
    private String app_id;
    private String app_key;
    private String refresh_token;

    public RefreshTokenRequestModel() {
    }

    public RefreshTokenRequestModel(String refreshToken) {
        this.app_id = ClientConfigs.TELEPATHY_APP_ID;
        this.app_key = ClientConfigs.TELEPATHY_APP_KEY;
        this.refresh_token = refreshToken;
    }
}

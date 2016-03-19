package com.atahani.telepathy.model;

/**
 * Update profile request model
 */
public class UpdateProfileRequestModel {
    private String username;
    private String display_name;
    private String locale;
    private String theme;

    public UpdateProfileRequestModel() {
    }


    public UpdateProfileRequestModel(String username, String displayName, String locale, String theme) {
        this.username = username;
        this.display_name = displayName;
        this.locale = locale;
        this.theme = theme;
    }

    public UpdateProfileRequestModel(String displayName, String locale, String theme) {
        this.display_name = displayName;
        this.locale = locale;
        this.theme = theme;
    }
}

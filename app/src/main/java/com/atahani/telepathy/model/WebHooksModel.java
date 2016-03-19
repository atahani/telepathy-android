package com.atahani.telepathy.model;

/**
 * Web Hooks model to send logs to slack ROBOT
 */
public class WebHooksModel {
    private String text;
    private String icon_emoji;

    public WebHooksModel() {
    }

    public WebHooksModel(String text, String icon_emoji) {
        this.text = text;
        this.icon_emoji = icon_emoji;
    }
}

package com.atahani.telepathy.model;

/**
 * Publish Telepathy Request
 */
public class PublishTelepathyRequestModel {
    private String to_user;
    private String body;
    private int expire_in_min;

    public PublishTelepathyRequestModel() {
    }

    public PublishTelepathyRequestModel(String toUserId, String body, int expireInMin) {
        this.to_user = toUserId;
        this.body = body;
        this.expire_in_min = expireInMin;
    }
}

package com.atahani.telepathy.model;

import java.util.Date;

/**
 * Telepathy model
 * used in Retrofit as response
 */
public class TelepathyModel {
    public String id;
    public UserModel to_user;
    public String body;
    public Date created_at;
    public Date expire_at;
}

package com.atahani.telepathy.model;

/**
 * response model in sign in request
 * contain token information and user profile
 */
public class AuthorizeResponseModel {
    public TokenModel token;
    public UserProfileModel user_profile;
    public boolean is_in_sign_up_mode;
}


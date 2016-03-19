package com.atahani.telepathy.model;

/**
 * Username checking response model
 * inform to username is valid or not
 */
public class UsernameCheckingResponse {
    public boolean is_valid;
    public boolean is_unique;
    public String username;
}

package com.atahani.telepathy.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * User Model Realm for save users profile data into realmDB
 */
public class UserModelRealm extends RealmObject {

    @PrimaryKey
    private String userId;
    private String username;
    private String displayName;
    private String imageUrl;
    private String theme;
    private boolean isFriend;
    private long numberOfUserMessageFromServer;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean friend) {
        isFriend = friend;
    }

    public long getNumberOfUserMessageFromServer() {
        return numberOfUserMessageFromServer;
    }

    public void setNumberOfUserMessageFromServer(long numberOfUserMessageFromServer) {
        this.numberOfUserMessageFromServer = numberOfUserMessageFromServer;
    }
}

package com.atahani.telepathy.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import java.util.Date;

/**
 * Telepathy realm model
 * used for show in telepathy recycler view
 */
public class TelepathyModelRealm  extends RealmObject {

    @PrimaryKey
    private String telepathyId;
    private String withUserId;
    private String withUserUsername;
    private String withUserDisplayName;
    private String withUserImageUrl;
    private String withUserTheme;
    private String body;
    private Date createdAt;
    private Date expireAt;


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public String getTelepathyId() {
        return telepathyId;
    }

    public void setTelepathyId(String telepathyId) {
        this.telepathyId = telepathyId;
    }

    public String getWithUserDisplayName() {
        return withUserDisplayName;
    }

    public void setWithUserDisplayName(String withUserDisplayName) {
        this.withUserDisplayName = withUserDisplayName;
    }

    public String getWithUserId() {
        return withUserId;
    }

    public void setWithUserId(String withUserId) {
        this.withUserId = withUserId;
    }

    public String getWithUserImageUrl() {
        return withUserImageUrl;
    }

    public void setWithUserImageUrl(String withUserImageUrl) {
        this.withUserImageUrl = withUserImageUrl;
    }

    public String getWithUserTheme() {
        return withUserTheme;
    }

    public void setWithUserTheme(String withUserTheme) {
        this.withUserTheme = withUserTheme;
    }

    public String getWithUserUsername() {
        return withUserUsername;
    }

    public void setWithUserUsername(String withUserUsername) {
        this.withUserUsername = withUserUsername;
    }
}

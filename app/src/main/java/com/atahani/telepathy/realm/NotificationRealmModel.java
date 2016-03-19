package com.atahani.telepathy.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Model to store notification id for notification realm model
 */
public class NotificationRealmModel extends RealmObject {

    @PrimaryKey
    private int notificationId;

    private String fromUserId;

    private String tagType;

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }
}

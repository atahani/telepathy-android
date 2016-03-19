package com.atahani.telepathy.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * used for manage notifications
 */
public class UserMessageNotificationRealmModel extends RealmObject {

    @PrimaryKey
    private int notificationId;
    private String fromUserId;
    private int numberOfNotification;
    private String notificationBody;

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

    public int getNumberOfNotification() {
        return numberOfNotification;
    }

    public void setNumberOfNotification(int numberOfNotification) {
        this.numberOfNotification = numberOfNotification;
    }

    public String getNotificationBody() {
        return notificationBody;
    }

    public void setNotificationBody(String notificationBody) {
        this.notificationBody = notificationBody;
    }
}

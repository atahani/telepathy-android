package com.atahani.telepathy.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import java.util.Date;

/**
 * Message model realm contain message and telepathy in one model
 * used for show recycler view of
 */
public class MessageModelRealm extends RealmObject {

    @PrimaryKey
    private String messageId;
    private String withUserId;
    private String bodySend;
    private String bodyReceive;
    private boolean youAreFirst;
    private Date matchedAt;
    private String matchedWithTelepathyId;
    private int matchedInSec;
    private boolean isReceive;
    private boolean isRead;
    private boolean isSendReadSignal;
    private Date updatedAt;

    public String getBodyReceive() {
        return bodyReceive;
    }

    public void setBodyReceive(String bodyReceive) {
        this.bodyReceive = bodyReceive;
    }

    public String getBodySend() {
        return bodySend;
    }

    public void setBodySend(String bodySend) {
        this.bodySend = bodySend;
    }

    public Date getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(Date matchedAt) {
        this.matchedAt = matchedAt;
    }

    public int getMatchedInSec() {
        return matchedInSec;
    }

    public void setMatchedInSec(int matchedInSec) {
        this.matchedInSec = matchedInSec;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getWithUserId() {
        return withUserId;
    }

    public void setWithUserId(String withUserId) {
        this.withUserId = withUserId;
    }

    public boolean isYouAreFirst() {
        return youAreFirst;
    }

    public void setYouAreFirst(boolean youAreFirst) {
        this.youAreFirst = youAreFirst;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isReceive() {
        return isReceive;
    }

    public void setIsReceive(boolean isReceive) {
        this.isReceive = isReceive;
    }

    public String getMatchedWithTelepathyId() {
        return matchedWithTelepathyId;
    }

    public void setMatchedWithTelepathyId(String matchedWithTelepathyId) {
        this.matchedWithTelepathyId = matchedWithTelepathyId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSendReadSignal() {
        return isSendReadSignal;
    }

    public void setIsSendReadSignal(boolean isSendReadSignal) {
        this.isSendReadSignal = isSendReadSignal;
    }
}

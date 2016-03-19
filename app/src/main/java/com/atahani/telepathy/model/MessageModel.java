package com.atahani.telepathy.model;

import java.util.Date;

/**
 * Message Model used in Retrofit
 */
public class MessageModel {
    public String id;
    public String with_user_id;
    public String body_send;
    public String body_receive;
    public boolean you_are_first;
    public String matched_with_telepathy_id;
    public Date matched_at;
    public int matched_in_sec;
    public boolean is_receive;
    public boolean is_read;
    public boolean is_send_read_signal;
    public Date updated_at;
}

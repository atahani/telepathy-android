package com.atahani.telepathy.model;

import java.util.List;

/**
 * the messages with total and page number as result
 */
public class MessagesModel {
    public long total;
    public int page;
    public List<MessageModel> data;
}

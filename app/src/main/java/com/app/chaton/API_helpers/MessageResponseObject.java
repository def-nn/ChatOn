package com.app.chaton.API_helpers;

import java.util.List;

public class MessageResponseObject extends ResponseObject{
    private List<Message> data;
    public List<Message> getData() { return this.data; }
}

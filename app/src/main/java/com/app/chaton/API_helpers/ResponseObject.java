package com.app.chaton.API_helpers;

public abstract class ResponseObject {

    private int status;
    private String answer;

    public int getStatus() { return this.status; }
    public String getAnswer() { return this.answer; }
    public Object getData() { return this.answer; }
}

package com.app.chaton.API_helpers;


import java.util.HashMap;

public class ResponseObject {
    /**
     *  Объект, в который конвертируется полученный от сервера ответ
     */

    private int status;
    private String answer;
    private HashMap<String, String> data;

    public int getStatus() { return this.status; }
    public String getAnswer() { return this.answer; }
    public HashMap<String, String> getData() { return this.data; }
}

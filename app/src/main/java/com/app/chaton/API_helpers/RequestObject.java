package com.app.chaton.API_helpers;


public class RequestObject {

    private Object data;
    private String signature;

    public RequestObject(Object data) {
        this.data = data;
        this.signature = createSignature();
    }

    private String createSignature() {
        // TODO
        return null;
    }
}

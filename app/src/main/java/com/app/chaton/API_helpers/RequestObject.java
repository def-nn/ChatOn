package com.app.chaton.API_helpers;

import com.app.chaton.Encryptor;

public class RequestObject {
    /**
     *  Класс используемый при отправке данных на сервер
     */

    private Object data;
    private String signature;

    public RequestObject(Object data) {
        this.data = data;
    }

    public RequestObject(Object data, long id, String secret_key) {
        this.data = data;
        this.signature = createSignature(id, secret_key);
    }

    private String createSignature(long id, String secret_key) {
        String signature_base = "sig" + id + secret_key;
        return Encryptor.md5(Encryptor.sha1(Encryptor.base64(signature_base)));
    }
}

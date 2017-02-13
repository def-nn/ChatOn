package com.app.chaton.API_helpers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class RequestObject {

    private Object data;
    private String signature;

    public RequestObject(Object data) {
        this.data = data;
    }

    public RequestObject(Object data, long id, String secret_key) {
        this.data = data;

        String sign_base = "sig" + secret_key + id;
        byte[] base64 = Base64.encodeBase64(sign_base.getBytes());
        String md5 = new String(Hex.encodeHex(DigestUtils.md5(base64)));
        this.signature = new String(Hex.encodeHex(DigestUtils.sha1(md5)));
    }

}

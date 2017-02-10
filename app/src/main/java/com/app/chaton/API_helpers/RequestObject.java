package com.app.chaton.API_helpers;


import android.util.Base64;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import com.app.chaton.PreferenceHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RequestObject {
    /**
     *  Класс используемый при отправке данных на сервер
     */

    private Object data;
    private String signature;

    public RequestObject(Object data, PreferenceHelper helper) {
        this.data = data;
        this.signature = createSignature(helper);
    }

    private String createSignature(PreferenceHelper helper) {
        if (!helper.isAuth()) return null;

        String secret_key = helper.getSecretKey();
        Long id = helper.getId();
        String signature_base = "sig" + secret_key + id;

//        return md5(DigestUtils.sha1Hex(Base64.encodeToString(signature_base.getBytes(), Base64.DEFAULT)));
        return  signature_base;
    }

    private String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

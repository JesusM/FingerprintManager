package com.jesusm.jfingerprintmanager.encryption;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;

public class Base64Encoder implements Encoder {
    @Override
    public String encrypt(String message, Cipher cipher) {
        try {
            byte[] encryptedBytes = cipher.doFinal(message.getBytes("UTF-8"));
            byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

            return encryptBase64(encryptedBytes) + ":" + encryptBase64(ivBytes);
        } catch (BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException | InvalidParameterSpecException e) {
            return null;
        }
    }

    private String encryptBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}

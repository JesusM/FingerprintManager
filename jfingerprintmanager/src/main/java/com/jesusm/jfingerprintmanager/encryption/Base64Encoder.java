package com.jesusm.jfingerprintmanager.encryption;

import android.util.Base64;

public class Base64Encoder implements Encoder {

    @Override
    public String encode(byte[] messageToEncrypt) {
        return Base64.encodeToString(messageToEncrypt, Base64.DEFAULT);
    }

    @Override
    public byte[] decode(String messageToDecrypt) {
        return Base64.decode(messageToDecrypt, Base64.DEFAULT);
    }
}

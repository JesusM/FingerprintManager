package com.jesusm.jfingerprintmanager.encryption;

import javax.crypto.Cipher;

public interface Encoder {
    String encrypt(String message, Cipher cipher);
}

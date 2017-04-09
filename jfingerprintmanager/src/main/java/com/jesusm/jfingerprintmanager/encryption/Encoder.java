package com.jesusm.jfingerprintmanager.encryption;

public interface Encoder {
    String encode(byte[] message);
    byte[] decode(String messageToDecode);
}

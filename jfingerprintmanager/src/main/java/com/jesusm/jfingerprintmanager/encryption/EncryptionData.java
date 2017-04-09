package com.jesusm.jfingerprintmanager.encryption;

import com.jesusm.jfingerprintmanager.utils.TextUtils;

class EncryptionData {
    private static final String SEPARATOR = ":";

    private String encryptedMessage, encryptedIVs;
    private Encoder encoder;

    EncryptionData(byte[] message, byte[] IVs, Encoder encoder) {
        this.encoder = encoder;
        this.encryptedMessage = encoder.encode(message);
        this.encryptedIVs = encoder.encode(IVs);
    }

    EncryptionData(String encryptedMessage, Encoder encoder) {
        this.encoder = encoder;
        String[] parameters = encryptedMessage.split(SEPARATOR);
        this.encryptedMessage = parameters[0];
        if (parameters.length > 1) {
            this.encryptedIVs = parameters[1];
        }
    }

    byte[] message()
    {
        return encoder.decode(encryptedMessage);
    }

    byte[] getIVs() {
        return encoder.decode(encryptedIVs);
    }

    String printEncryptedInformation() {
        return encryptedMessage + SEPARATOR + encryptedIVs;
    }

    boolean dataIsCorrect() {
        TextUtils textUtils = new TextUtils();
        return !textUtils.isEmpty(encryptedMessage) && !textUtils.isEmpty(encryptedIVs);
    }
}

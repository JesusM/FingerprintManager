package com.jesusm.jfingerprintmanager.encryption;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.BaseFingerprintManager;
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager;
import com.jesusm.jfingerprintmanager.base.ui.System;
import com.jesusm.jfingerprintmanager.encryption.ui.FingerprintEncryptionDialogFragment;
import com.jesusm.jfingerprintmanager.utils.TextUtils;

import javax.crypto.Cipher;

public class EncryptionManager extends BaseFingerprintManager{

    private Encoder encoder;
    private final TextUtils textUtils;

    public EncryptionManager(FingerprintAssetsManager fingerprintAssetsManager, System system,
                             Encoder encoder, TextUtils textUtils) {
        super(fingerprintAssetsManager, system);
        this.encoder = encoder;
        this.textUtils = textUtils;
    }

    public void encrypt(@NonNull final String messageToEncrypt,
                        @NonNull final JFingerprintManager.EncryptionCallback callback,
                        @NonNull final FragmentManager fragmentManager) {
        if (textUtils.isEmpty(messageToEncrypt)) {
            callback.onEncryptionFailed();
        }

        fingerprintAssetsManager.initSecureDependencies(new JFingerprintManager.InitialisationCallback() {
            @Override
            public void onErrorFingerprintNotInitialised() {
                callback.onEncryptionFailed();
            }

            @Override
            public void onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable();
            }

            @Override
            public void onInitialisationSuccessfullyCompleted() {
                JFingerprintManager.EncryptionAuthenticatedCallback encryptionCallback = new JFingerprintManager.EncryptionAuthenticatedCallback() {
                    @Override
                    public void onAuthenticationSuccess(@NonNull FingerprintManagerCompat.CryptoObject cryptoObject) {
                        Cipher cipher = cryptoObject.getCipher();
                        String encryptedMessage = encoder.encrypt(messageToEncrypt, cipher);
                        if (encryptedMessage != null) {
                            callback.onEncryptionSuccess(encryptedMessage);
                        } else {
                            callback.onEncryptionFailed();
                        }
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        callback.onFingerprintNotRecognized();
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(String help) {
                        callback.onAuthenticationFailedWithHelp(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        callback.onFingerprintNotAvailable();
                    }

                };

                FingerprintEncryptionDialogFragment.Builder builder = new FingerprintEncryptionDialogFragment.Builder();
                showFingerprintDialog(builder, fragmentManager, encryptionCallback);
            }

            @Override
            public void onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable();
            }
        });
    }
}

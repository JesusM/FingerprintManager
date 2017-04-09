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

import java.io.UnsupportedEncodingException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;

public class EncryptionManager extends BaseFingerprintManager{

    private Encoder encoder;
    private final TextUtils textUtils;

    public EncryptionManager(FingerprintAssetsManager fingerprintAssetsManager, System system,
                             Encoder encoder) {
        super(fingerprintAssetsManager, system);
        this.encoder = encoder;
        this.textUtils = new TextUtils();
    }

    public void encrypt(@NonNull final String messageToEncrypt,
                        @NonNull final JFingerprintManager.EncryptionCallback callback,
                        @NonNull final FragmentManager fragmentManager) {
        if (textUtils.isEmpty(messageToEncrypt)) {
            callback.onEncryptionFailed();
            return;
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
                        try {
                            byte[] messageToEncryptBytes = cipher.doFinal(messageToEncrypt.getBytes("UTF-8"));
                            byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

                            EncryptionData encryptedMessage = new EncryptionData(messageToEncryptBytes, ivBytes, encoder);
                            callback.onEncryptionSuccess(encryptedMessage.printEncryptedInformation());
                        } catch (UnsupportedEncodingException | InvalidParameterSpecException |
                                BadPaddingException | IllegalBlockSizeException e) {
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

    public void decrypt(@NonNull final String messageToDecrypt,
                        @NonNull final JFingerprintManager.DecryptionCallback callback,
                        @NonNull final FragmentManager fragmentManager) {
        if (textUtils.isEmpty(messageToDecrypt)) {
            callback.onDecryptionFailed();
            return;
        }

        EncryptionData decryptionData = new EncryptionData(messageToDecrypt, encoder);

        if (!decryptionData.dataIsCorrect()) {
            callback.onDecryptionFailed();
            return;
        }

        fingerprintAssetsManager.initSecureDependenciesForDecryption(new JFingerprintManager.InitialisationCallback() {
            @Override
            public void onErrorFingerprintNotInitialised() {
                callback.onDecryptionFailed();
            }

            @Override
            public void onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable();
            }

            @Override
            public void onInitialisationSuccessfullyCompleted() {
                JFingerprintManager.EncryptionAuthenticatedCallback decryptionCallback = new JFingerprintManager.EncryptionAuthenticatedCallback() {
                    @Override
                    public void onAuthenticationSuccess(@NonNull FingerprintManagerCompat.CryptoObject cryptoObject) {
                        try {
                            EncryptionData decryptionData = new EncryptionData(messageToDecrypt, encoder);
                            Cipher cipher = cryptoObject.getCipher();

                            byte[] encryptedMessage = decryptionData.message();
                            byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessage);
                            String decryptedMessage = new String(decryptedMessageBytes);

                            callback.onDecryptionSuccess(decryptedMessage);
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            callback.onDecryptionFailed();
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
                showFingerprintDialog(builder, fragmentManager, decryptionCallback);
            }

            @Override
            public void onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable();
            }
        }, decryptionData.getIVs());
    }
}

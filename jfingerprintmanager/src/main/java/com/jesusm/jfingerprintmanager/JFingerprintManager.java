package com.jesusm.jfingerprintmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.authentication.AuthenticationManager;
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager;
import com.jesusm.jfingerprintmanager.base.ui.System;
import com.jesusm.jfingerprintmanager.base.ui.SystemImpl;
import com.jesusm.jfingerprintmanager.encryption.Base64Encoder;
import com.jesusm.jfingerprintmanager.encryption.Encoder;
import com.jesusm.jfingerprintmanager.encryption.EncryptionManager;

public class JFingerprintManager {
    private EncryptionManager encryptionManager;
    private AuthenticationManager authenticationManager;

    @VisibleForTesting
    public JFingerprintManager(System system, FingerprintAssetsManager fingerprintAssetsManager,
                               Encoder encoder) {
        this.encryptionManager = new EncryptionManager(fingerprintAssetsManager, system, encoder);
        this.authenticationManager = new AuthenticationManager(fingerprintAssetsManager, system);
    }

    public JFingerprintManager(Context context, @NonNull String keyStoreAlias) {
        this(new SystemImpl(), new FingerprintAssetsManager(context, keyStoreAlias),
                new Base64Encoder());
    }

    /**
     * Set style for the UI component displayed.
     *
     * @param style Id resource that points to the style used for the UI component.
     */
    public void setAuthenticationDialogStyle(@StyleRes int style) {
        authenticationManager.setAuthenticationDialogStyle(style);
        encryptionManager.setAuthenticationDialogStyle(style);
    }

    /**
     * This method encrypts the text given by messageToEncrypt parameter, using Android Fingerprint APIs.
     *  @param messageToEncrypt Text to be encrypted.
     * @param callback         Callback that receives events produced in encryption process.
     * @param fragmentManager  FragmentManager to allow displaying UI component (DialogFragment).
     */
    public void encrypt(@NonNull final String messageToEncrypt,
                        @NonNull final EncryptionCallback callback,
                        @NonNull final FragmentManager fragmentManager) {
        encryptionManager.encrypt(messageToEncrypt, callback, fragmentManager);
    }

    /**
     * This method encrypts the text given by messageToEncrypt parameter, using Android Fingerprint APIs.
     *  @param messageToDecrypt Text to be decrypted.
     * @param callback         Callback that receives events produced in encryption process.
     * @param fragmentManager  FragmentManager to allow displaying UI component (DialogFragment).
     */
    public void decrypt(@NonNull final String messageToDecrypt,
                        @NonNull final DecryptionCallback callback,
                        @NonNull final FragmentManager fragmentManager) {
        encryptionManager.decrypt(messageToDecrypt, callback, fragmentManager);
    }

    /**
     * Authenticate user via Android Fingerprint APIs or manual password (if explicitly chosen).
     *
     * @param authenticationCallback Callback that receives events produced in the authentication process.
     * @param fragmentManager        FragmentManager to display UI component (a DialogFragment).
     */
    public void startAuthentication(@NonNull final AuthenticationCallback authenticationCallback,
                                    @NonNull final FragmentManager fragmentManager) {
        authenticationManager.startAuthentication(authenticationCallback, fragmentManager);
    }

    public interface FingerprintBaseCallback {
        void onFingerprintNotRecognized();

        void onAuthenticationFailedWithHelp(String help);

        void onFingerprintNotAvailable();

        void onCancelled();
    }

    public interface DecryptionCallback extends FingerprintBaseCallback{
        void onDecryptionSuccess(String messageDecrypted);

        void onDecryptionFailed();
    }

    public interface EncryptionCallback extends FingerprintBaseCallback{
        void onEncryptionSuccess(String messageEncrypted);

        void onEncryptionFailed();
    }

    public interface AuthenticationCallback extends JFingerprintManager.FingerprintBaseCallback {
        void onAuthenticationSuccess();

        void onSuccessWithManualPassword(@NonNull String password);
    }

    public interface EncryptionAuthenticatedCallback extends FingerprintBaseCallback {
        void onAuthenticationSuccess(FingerprintManagerCompat.CryptoObject cryptoObject);
    }

    public interface InitialisationCallback extends FingerprintAvailabilityCallback {
        void onErrorFingerprintNotInitialised();

        void onErrorFingerprintNotEnrolled();

        void onInitialisationSuccessfullyCompleted();
    }

    public interface FingerprintAvailabilityCallback {
        void onFingerprintNotAvailable();
    }
}

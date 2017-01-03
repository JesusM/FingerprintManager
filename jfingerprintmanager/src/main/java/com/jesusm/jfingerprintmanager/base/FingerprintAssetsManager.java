package com.jesusm.jfingerprintmanager.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;
import com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_NOT_AVAILABLE;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_NOT_ENROLLED;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.LOCK_SCREEN_RESET_OR_DISABLED;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.NEW_FINGERPRINT_ENROLLED;

public class FingerprintAssetsManager {
    private final Context context;
    private FingerprintHardware fingerprintHardware;
    private KeyStoreManager keyStoreManager;
    private String keyStoreAlias;
    private FingerprintErrorState errorState;

    @VisibleForTesting
    public FingerprintAssetsManager(Context context, FingerprintHardware fingerprintHardware, KeyStoreManager keyStoreManager, String keyStoreAlias) {
        this.fingerprintHardware = fingerprintHardware;
        this.keyStoreManager = keyStoreManager;
        this.context = context;
        this.keyStoreAlias = keyStoreAlias;
    }

    public FingerprintAssetsManager(Context context, String keyStoreAlias) {
        this(context, new FingerprintHardware(context), new KeyStoreManager(context), keyStoreAlias);
    }

    public void initSecureDependencies(@Nullable JFingerprintManager.InitialisationCallback callback) {
        if (!isFingerprintAuthAvailable()) {
            errorState = FINGERPRINT_NOT_AVAILABLE;
            if (callback != null) {
                callback.onFingerprintNotAvailable();
            }
            logError(errorState.getErrorMessage());
            return;
        }

        try {
            keyStoreManager.createKeyStore();
        } catch (KeyStoreException e) {
            errorState = FINGERPRINT_INITIALISATION_ERROR;
            if (callback != null) {
                callback.onErrorFingerprintNotInitialised();
            }
            logError(errorState.getErrorMessage());
            return;
        }

        try {
            keyStoreManager.createKeyGenerator();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            errorState = FINGERPRINT_INITIALISATION_ERROR;
            if (callback != null) {
                callback.onErrorFingerprintNotInitialised();
            }
            logError(errorState.getErrorMessage());
            return;
        }

        try {
            keyStoreManager.createCipher();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            errorState = FINGERPRINT_INITIALISATION_ERROR;
            if (callback != null) {
                callback.onErrorFingerprintNotInitialised();
            }
            logError(errorState.getErrorMessage());
            return;
        }

        if (!keyStoreManager.isFingerprintEnrolled()) {
            errorState = FINGERPRINT_NOT_ENROLLED;
            if (callback != null) {
                callback.onErrorFingerprintNotEnrolled();
            }
            logError(errorState.getErrorMessage());
            return;
        }

        try {
            keyStoreManager.createKey(keyStoreAlias, true);
            keyStoreManager.initDefaultCipher();
        } catch (RuntimeException e) {
            errorState = LOCK_SCREEN_RESET_OR_DISABLED;
            if (callback != null) {
                callback.onErrorFingerprintNotInitialised();
            }
            logError(LOCK_SCREEN_RESET_OR_DISABLED.getErrorMessage());
            return;
        } catch (KeyStoreManager.NewFingerprintEnrolledException e) {
            errorState = NEW_FINGERPRINT_ENROLLED;
            logError(errorState.getErrorMessage());
        }

        boolean isCipherAvailable = keyStoreManager.isCipherAvailable();

        if (callback != null) {
            if (isCipherAvailable) {
                callback.onInitialisationSuccessfullyCompleted();
            } else {
                callback.onErrorFingerprintNotInitialised();
            }
        }
    }

    private boolean isFingerprintAuthAvailable() {
        return fingerprintHardware.isFingerprintAuthAvailable();
    }

    public void createKey(boolean invalidatedByBiometricEnrollment) {
        keyStoreManager.createKey(keyStoreAlias, invalidatedByBiometricEnrollment);
    }

    private void logError(@StringRes int message) {
        if (message != -1) {
            logError(context.getString(message));
        }
    }

    private void logError(String message) {
        Log.e(getClass().getSimpleName(), message);
    }

    public FingerprintHardware getFingerprintHardware() {
        return fingerprintHardware;
    }

    public Cipher getDefaultCipher() {
        return keyStoreManager.getDefaultCipher();
    }

    public FingerprintErrorState getErrorState() {
        return errorState;
    }
}

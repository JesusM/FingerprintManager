package com.jesusm.jfingerprintmanager.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;
import com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_NOT_AVAILABLE;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.FINGERPRINT_NOT_ENROLLED;
import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.NEW_FINGERPRINT_ENROLLED;

public class FingerprintAssetsManager {
    private final Context context;
    private FingerprintHardware fingerprintHardware;
    private KeyStoreManager keyStoreManager;
    private String keyStoreAlias;
    private FingerprintErrorState errorState;
    private Cipher cipher;

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
        initSecureDependenciesForDecryption(callback, null);
    }

    public void initSecureDependenciesForDecryption(@Nullable JFingerprintManager.InitialisationCallback callback,
                                                    @Nullable byte[] IVs) {
        initSecureDependenciesWithIVs(callback, IVs);
    }

    private void initSecureDependenciesWithIVs(@Nullable JFingerprintManager.InitialisationCallback callback,
                                               @Nullable byte[] IVs) {
        if (!isFingerprintAuthAvailable()) {
            handleError(callback, FINGERPRINT_NOT_AVAILABLE);
            return;
        }

        try {
            keyStoreManager.createKeyGenerator();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            handleError(callback, FINGERPRINT_INITIALISATION_ERROR);
            return;
        }

        if (!keyStoreManager.isFingerprintEnrolled()) {
            handleError(callback, FINGERPRINT_NOT_ENROLLED);
            return;
        }

        try {
            if (IVs == null) {
                cipher = keyStoreManager.initDefaultCipher(keyStoreAlias);
            } else {
                cipher = keyStoreManager.initCipherForDecryption(keyStoreAlias, IVs);
            }
        } catch (KeyStoreManager.NewFingerprintEnrolledException e) {
            handleError(callback, NEW_FINGERPRINT_ENROLLED);
            return;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                KeyStoreManager.InitialisationException e) {
            handleError(callback, FINGERPRINT_INITIALISATION_ERROR);
            return;
        }

        boolean isCipherAvailable = cipher != null;

        if (callback != null) {
            if (isCipherAvailable) {
                callback.onInitialisationSuccessfullyCompleted();
            } else {
                handleError(callback, FINGERPRINT_INITIALISATION_ERROR);
            }
        }
    }

    private void handleError(@Nullable JFingerprintManager.InitialisationCallback callback,
                             FingerprintErrorState errorState) {
        this.errorState = errorState;
        logError(errorState.getErrorMessage());

        switch (errorState) {
            case FINGERPRINT_NOT_AVAILABLE:
                if (callback != null) {
                    callback.onFingerprintNotAvailable();
                }
                break;
            case FINGERPRINT_INITIALISATION_ERROR:
                if (callback != null) {
                    callback.onErrorFingerprintNotInitialised();
                }
                break;
            case LOCK_SCREEN_RESET_OR_DISABLED:
            case FINGERPRINT_NOT_ENROLLED:
                if (callback != null) {
                    callback.onErrorFingerprintNotEnrolled();
                }
                break;
        }
    }

    private boolean isFingerprintAuthAvailable() {
        return fingerprintHardware.isFingerprintAuthAvailable();
    }

    public void createKey(boolean invalidatedByBiometricEnrollment) {
        try {
            keyStoreManager.createKey(keyStoreAlias, invalidatedByBiometricEnrollment);
        } catch (KeyStoreManager.InitialisationException e) {
            logError(e.getMessage());
        }
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

    public FingerprintErrorState getErrorState() {
        return errorState;
    }

    public FingerprintManagerCompat.CryptoObject getCryptoObject() {
        return new FingerprintManagerCompat.CryptoObject(cipher);
    }
}

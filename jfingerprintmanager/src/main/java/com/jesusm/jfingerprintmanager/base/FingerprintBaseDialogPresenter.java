package com.jesusm.jfingerprintmanager.base;


import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;
import com.jesusm.jfingerprintmanager.base.model.FingerprintManagerCancellationSignal;

import javax.crypto.Cipher;

import static com.jesusm.jfingerprintmanager.base.FingerprintBaseDialogPresenter.BaseStage.FINGERPRINT;

public abstract class FingerprintBaseDialogPresenter extends
        FingerprintManagerCompat.AuthenticationCallback {

    private FingerprintManagerCancellationSignal cancellationSignal;
    protected FingerprintHardware fingerprintHardware;
    @Nullable
    private Cipher defaultCipher;

    public interface Stage {
        String id();
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum BaseStage implements Stage {
        FINGERPRINT() {
            @Override
            public String id() {
                return "fingerprint";
            }
        }
    }

    protected Stage stage = FINGERPRINT;
    @NonNull
    protected View view;

    public FingerprintBaseDialogPresenter(@NonNull View view) {
        this.view = view;
        cancellationSignal = new FingerprintManagerCancellationSignal();
    }

    public void pause() {
        cancelFingerprintAuthenticationListener();
    }

    protected void cancelFingerprintAuthenticationListener() {
        cancellationSignal.cancel();
    }

    public void onViewShown() {
        updateStage();
    }

    @CallSuper
    protected void updateStage() {
        if (stage.id().equals(FINGERPRINT.id())) {
            displayFingerprint();
        }
    }

    private void displayFingerprint() {
        view.onFingerprintDisplayed();
        startAuthenticationListener();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void setFingerprintHardware(@NonNull FingerprintHardware fingerprintHardware,
                                       @Nullable Cipher defaultCipher) {
        this.fingerprintHardware = fingerprintHardware;
        this.defaultCipher = defaultCipher;
    }

    @SuppressWarnings("MissingPermission")
    private void startAuthenticationListener() {
        cancellationSignal.start();

        // As soon as this is called, we are listening for fingerprint introduction.
        FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(defaultCipher);
        fingerprintHardware.authenticate(cryptoObject, 0, cancellationSignal.getCancellationSignal(), this, null);
    }

    public void setCancellationSignal(FingerprintManagerCancellationSignal cancellationSignal) {
        this.cancellationSignal = cancellationSignal;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!cancellationSignal.isCancelled()) {
            view.onFingerprintNotRecognized();
            close();
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        view.onAuthenticationFailedWithHelp(helpString.toString());
        close();
    }

    @Override
    public void onAuthenticationFailed() {
        view.onFingerprintNotRecognized();
        close();
    }

    protected void close() {
        view.close();
    }

    public interface View extends JFingerprintManager.FingerprintBaseCallback {
        void onFingerprintDisplayed();

        void close();
    }
}

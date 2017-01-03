package com.jesusm.jfingerprintmanager.encryption.presenter;

import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.base.FingerprintBaseDialogPresenter;

public class FingerprintEncryptionDialogPresenter extends FingerprintBaseDialogPresenter {
    @NonNull
    protected View view;

    public FingerprintEncryptionDialogPresenter(@NonNull View view) {
        super(view);
        this.view = view;
    }

    @Override
    public void onViewShown() {
        if (!fingerprintHardware.isFingerprintAuthAvailable()) {
            close();
            return;
        }

        super.onViewShown();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        view.onAuthenticationSucceed(result.getCryptoObject());
        close();
    }

    public interface View extends FingerprintBaseDialogPresenter.View {
        void onAuthenticationSucceed(FingerprintManagerCompat.CryptoObject cryptoObject);
    }
}

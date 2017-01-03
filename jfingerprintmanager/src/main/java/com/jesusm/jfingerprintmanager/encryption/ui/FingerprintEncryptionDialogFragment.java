package com.jesusm.jfingerprintmanager.encryption.ui;

import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment;
import com.jesusm.jfingerprintmanager.encryption.presenter.FingerprintEncryptionDialogPresenter;

public class FingerprintEncryptionDialogFragment extends FingerprintBaseDialogFragment<FingerprintEncryptionDialogPresenter> implements FingerprintEncryptionDialogPresenter.View {

    private JFingerprintManager.EncryptionAuthenticatedCallback callback;

    @Override
    public void setCallback(@NonNull JFingerprintManager.FingerprintBaseCallback callback) {
        super.setCallback(callback);
        this.callback = (JFingerprintManager.EncryptionAuthenticatedCallback) callback;
    }

    @Override
    public void onAuthenticationSucceed(FingerprintManagerCompat.CryptoObject cryptoObject) {
        callback.onAuthenticationSuccess(cryptoObject);
    }

    public static class Builder extends FingerprintBaseDialogFragment.Builder<FingerprintEncryptionDialogFragment, FingerprintEncryptionDialogPresenter> {

        @Override
        protected FingerprintEncryptionDialogFragment createDialogFragment() {
            return new FingerprintEncryptionDialogFragment();
        }

        @Override
        protected FingerprintEncryptionDialogPresenter createPresenter(FingerprintEncryptionDialogFragment dialogFragment) {
            return new FingerprintEncryptionDialogPresenter(dialogFragment);
        }
    }
}

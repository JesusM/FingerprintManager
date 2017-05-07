package com.jesusm.jfingerprintmanager.authentication;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.authentication.ui.FingerprintAuthenticationDialogFragment;
import com.jesusm.jfingerprintmanager.base.BaseFingerprintManager;
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager;
import com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState;
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment;
import com.jesusm.jfingerprintmanager.base.ui.System;

import static com.jesusm.jfingerprintmanager.base.model.FingerprintErrorState.LOCK_SCREEN_RESET_OR_DISABLED;

public class AuthenticationManager extends BaseFingerprintManager {

    public AuthenticationManager(FingerprintAssetsManager fingerprintAssetsManager, System system) {
        super(fingerprintAssetsManager, system);
    }

    public void startAuthentication(@NonNull final JFingerprintManager.AuthenticationCallback callback,
                             @NonNull final FragmentManager fragmentManager) {
        fingerprintAssetsManager.initSecureDependencies(new JFingerprintManager.InitialisationCallback() {
            @Override
            public void onErrorFingerprintNotInitialised() {
                callback.onFingerprintNotAvailable();
            }

            @Override
            public void onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable();
            }

            @Override
            public void onInitialisationSuccessfullyCompleted() {
                FingerprintAuthenticationDialogFragment.AuthenticationDialogCallback authenticationCallback =
                        new FingerprintAuthenticationDialogFragment.AuthenticationDialogCallback() {

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

                            @Override
                            public void onCancelled() {
                                callback.onCancelled();
                            }

                            @Override
                            public void onAuthenticationSuccess() {
                                callback.onAuthenticationSuccess();
                            }

                            @Override
                            public void onSuccessWithManualPassword(@NonNull String password) {
                                callback.onSuccessWithManualPassword(password);
                            }

                            @Override
                            public void createKey(boolean invalidatedByBiometricEnrollment) {
                                fingerprintAssetsManager.createKey(invalidatedByBiometricEnrollment);
                            }

                            @Override
                            public void onPasswordInserted(String password) {
                                callback.onSuccessWithManualPassword(password);
                            }
                        };

                FingerprintErrorState errorState = fingerprintAssetsManager.getErrorState();
                FingerprintBaseDialogFragment.Builder builder = new FingerprintAuthenticationDialogFragment.Builder()
                        .newFingerprintEnrolled(errorState == LOCK_SCREEN_RESET_OR_DISABLED);

                showFingerprintDialog(builder, fragmentManager, authenticationCallback);
            }

            @Override
            public void onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable();
            }
        });
    }

}

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

    public void startAuthentication(@NonNull final JFingerprintManager.AuthenticationCallback authenticationCallback,
                             @NonNull final FragmentManager fragmentManager) {
        fingerprintAssetsManager.initSecureDependencies(new JFingerprintManager.InitialisationCallback() {
            @Override
            public void onErrorFingerprintNotInitialised() {
                authenticationCallback.onFingerprintNotAvailable();
            }

            @Override
            public void onErrorFingerprintNotEnrolled() {
                authenticationCallback.onFingerprintNotAvailable();
            }

            @Override
            public void onInitialisationSuccessfullyCompleted() {
                FingerprintAuthenticationDialogFragment.AuthenticationDialogCallback callback =
                        new FingerprintAuthenticationDialogFragment.AuthenticationDialogCallback() {

                            @Override
                            public void onFingerprintNotRecognized() {
                                authenticationCallback.onFingerprintNotRecognized();
                            }

                            @Override
                            public void onAuthenticationFailedWithHelp(String help) {
                                authenticationCallback.onAuthenticationFailedWithHelp(help);
                            }

                            @Override
                            public void onFingerprintNotAvailable() {
                                authenticationCallback.onFingerprintNotAvailable();
                            }

                            @Override
                            public void onAuthenticationSuccess() {
                                authenticationCallback.onAuthenticationSuccess();
                            }

                            @Override
                            public void onSuccessWithManualPassword(@NonNull String password) {
                                authenticationCallback.onSuccessWithManualPassword(password);
                            }

                            @Override
                            public void createKey(boolean invalidatedByBiometricEnrollment) {
                                fingerprintAssetsManager.createKey(invalidatedByBiometricEnrollment);
                            }

                            @Override
                            public void onPasswordInserted(String password) {
                                authenticationCallback.onSuccessWithManualPassword(password);
                            }
                        };

                FingerprintErrorState errorState = fingerprintAssetsManager.getErrorState();
                FingerprintBaseDialogFragment.Builder builder = new FingerprintAuthenticationDialogFragment.Builder()
                        .newFingerprintEnrolled(errorState == LOCK_SCREEN_RESET_OR_DISABLED);

                showFingerprintDialog(builder, fragmentManager, callback);
            }

            @Override
            public void onFingerprintNotAvailable() {
                authenticationCallback.onFingerprintNotAvailable();
            }
        });
    }

}

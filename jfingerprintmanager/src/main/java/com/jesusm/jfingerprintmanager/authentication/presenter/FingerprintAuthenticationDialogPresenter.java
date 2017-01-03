package com.jesusm.jfingerprintmanager.authentication.presenter;


import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.base.FingerprintBaseDialogPresenter;

import static com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter.AuthenticationStage.NEW_FINGERPRINT_ENROLLED;
import static com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter.AuthenticationStage.PASSWORD;

public class FingerprintAuthenticationDialogPresenter extends FingerprintBaseDialogPresenter {

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    enum AuthenticationStage implements FingerprintBaseDialogPresenter.Stage {
        NEW_FINGERPRINT_ENROLLED {
            @Override
            public String id() {
                return "new_fingerprint";
            }
        },
        PASSWORD {
            @Override
            public String id() {
                return "password";
            }
        }
    }

    @NonNull
    protected View view;

    public FingerprintAuthenticationDialogPresenter(@NonNull View view) {
        super(view);
        this.view = view;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        view.onAuthenticationSucceed();
        close();
    }

    @Override
    public void onViewShown() {
        if (!fingerprintHardware.isFingerprintAuthAvailable()) {
            setStage(PASSWORD);
        }

        super.onViewShown();
    }

    @Override
    protected void updateStage() {
        String stageId = stage.id();
        if (stageId.equals(NEW_FINGERPRINT_ENROLLED.id()) || stageId.equals(PASSWORD.id())) {
            goToPassword();
            return;
        }

        super.updateStage();
    }

    private void goToPassword() {
        showPassword();
        cancelFingerprintAuthenticationListener();
    }

    private void showPassword() {
        view.onPasswordViewDisplayed(stage == NEW_FINGERPRINT_ENROLLED);
    }

    public void showPasswordClicked() {
        setStage(PASSWORD);
        updateStage();
    }

    public void onPasswordEntered(String password, boolean useFingerprintFuture) {
        if (stage == BaseStage.FINGERPRINT) {
            goToPassword();
        } else {
            verifyPassword(password, useFingerprintFuture);
        }
    }

    private void verifyPassword(String password, boolean useFingerprintFuture) {
        if (!isValidPassword(password)) {
            view.onPasswordEmpty();
            return;
        }

        if (stage == NEW_FINGERPRINT_ENROLLED) {
            view.saveUseFingerprintFuture(useFingerprintFuture);
            if (useFingerprintFuture) {
                // Re-create the key so that fingerprints including new ones are validated.
                view.createKey();
                setStage(BaseStage.FINGERPRINT);
            }
        }

        view.onPasswordInserted(password);
        close();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() > 0;
    }

    public void newFingerprintEnrolled() {
        stage = NEW_FINGERPRINT_ENROLLED;
    }

    public interface View extends FingerprintBaseDialogPresenter.View {
        void saveUseFingerprintFuture(boolean useFingerprintFuture);

        void createKey();

        void onPasswordInserted(String password);

        void onPasswordEmpty();

        void onAuthenticationSucceed();

        void onPasswordViewDisplayed(boolean newFingerprintEnrolled);
    }
}

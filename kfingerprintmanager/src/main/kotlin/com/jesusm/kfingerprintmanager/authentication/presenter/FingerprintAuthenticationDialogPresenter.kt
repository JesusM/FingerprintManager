package com.jesusm.kfingerprintmanager.authentication.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.base.ui.presenter.FingerprintBaseDialogPresenter

class FingerprintAuthenticationDialogPresenter(view: View) : FingerprintBaseDialogPresenter(view) {
    fun onPasswordEntered(password: String, useFingerprintFuture: Boolean) {
        if (stage === FingerprintBaseDialogPresenter.Stage.FINGERPRINT) {
            goToPassword()
        } else {
            verifyPassword(password, useFingerprintFuture)
        }
    }

    private fun goToPassword() {
        showPassword()
        cancelFingerprintAuthenticationListener()
    }

    private fun verifyPassword(password: String, useFingerprintFuture: Boolean) {
        if (!isValidPassword(password)) {
            (view as View).onPasswordEmpty()
            return
        }

        if (stage === Stage.NEW_FINGERPRINT_ENROLLED) {
            (view as View).saveUseFingerprintFuture(useFingerprintFuture)
            if (useFingerprintFuture) {
                // Re-create the key so that fingerprints including new ones are validated.
                view.createKey()
                stage = FingerprintBaseDialogPresenter.Stage.FINGERPRINT
            }
        }

        close()
        (view as View).onPasswordInserted(password)
    }

    private fun isValidPassword(password: String) : Boolean = password.isNullOrEmpty().not()

    private fun showPassword() {
        (view as View).onPasswordViewDisplayed(stage == Stage.NEW_FINGERPRINT_ENROLLED)
    }

    fun showPasswordClicked() {
        stage = Stage.PASSWORD
        updateStage()
    }

    fun newFingerprintEnrolled() {
        stage = Stage.NEW_FINGERPRINT_ENROLLED
    }

    override fun onViewShown() {
        if (!fingerprintHardware.isFingerprintAuthAvailable()) {
            stage = Stage.PASSWORD
        }

        super.onViewShown()
    }

    override fun updateStage() {
        when(stage)
        {
            Stage.NEW_FINGERPRINT_ENROLLED -> goToPassword()
            Stage.PASSWORD -> goToPassword()
            else -> super.updateStage()
        }
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
        close()
        (view as View).onAuthenticationSucceed()
    }

    interface View : FingerprintBaseDialogPresenter.View {
        fun saveUseFingerprintFuture(useFingerprintFuture: Boolean)

        fun createKey()

        fun onPasswordInserted(password: String)

        fun onPasswordEmpty()

        fun onAuthenticationSucceed()

        fun onPasswordViewDisplayed(newFingerprintEnrolled: Boolean)
    }
}
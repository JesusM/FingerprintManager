package com.jesusm.jfingerprintmanager.base.ui.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.jfingerprintmanager.JFingerprintManager
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.jfingerprintmanager.base.model.FingerprintManagerCancellationSignal

abstract class FingerprintBaseDialogPresenter(val view: View, var stage: Stage = FingerprintBaseDialogPresenter.Stage.FINGERPRINT,
                                              var cancellationSignal: FingerprintManagerCancellationSignal = FingerprintManagerCancellationSignal(),
                                              var fingerprintHardware: FingerprintHardware? = null,
                                              var cryptoObject: FingerprintManagerCompat.CryptoObject? = null) : FingerprintManagerCompat.AuthenticationCallback() {
    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    enum class Stage(val id: String) {
        FINGERPRINT("fingerprint"),
        NEW_FINGERPRINT_ENROLLED("newFingerprintEnrolled"),
        PASSWORD("password")
    }

    fun pause() {
        cancelFingerprintAuthenticationListener()
    }

    fun close() {
        view.close()
    }

    fun cancelFingerprintAuthenticationListener() {
        cancellationSignal.cancel()
    }

    open fun onViewShown() {
        updateStage()
    }

    open fun updateStage() {
        if (stage.id == Stage.FINGERPRINT.id) {
            displayFingerprint()
        }
    }

    private fun displayFingerprint() {
        view.onFingerprintDisplayed()
        startAuthenticationListener()
    }

    private fun startAuthenticationListener() {
        cancellationSignal.start()

        // As soon as this is called, we are listening for fingerprint introduction.
        cancellationSignal.cancellationSignal?.let {
            fingerprintHardware?.authenticate(cryptoObject, 0, it, this, null)
        }
    }

    fun setFingerprintHardware(hardware: FingerprintHardware, cryptoObject: FingerprintManagerCompat.CryptoObject?) {
        fingerprintHardware = hardware
        this.cryptoObject = cryptoObject
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        if (cancellationSignal.isCancelled().not()) {
            onAuthenticationFailed()
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        view.onAuthenticationFailedWithHelp(helpString.toString())
        close()
    }

    override fun onAuthenticationFailed() {
        view.onFingerprintNotRecognized()
        close()
    }

    fun onDialogCancelled() =
            view.onCancelled()

    interface View : JFingerprintManager.FingerprintBaseCallback {
        fun onFingerprintDisplayed()

        fun close()
    }
}
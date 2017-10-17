package com.jesusm.kfingerprintmanager.base.ui.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.kfingerprintmanager.base.model.FingerprintManagerCancellationSignal

abstract class FingerprintBaseDialogPresenter(val view: View, var stage: Stage = FingerprintBaseDialogPresenter.Stage.FINGERPRINT,
                                              var cancellationSignal: FingerprintManagerCancellationSignal = FingerprintManagerCancellationSignal()) : FingerprintManagerCompat.AuthenticationCallback() {
    lateinit var fingerprintHardware: FingerprintHardware
    private lateinit var cryptoObject: FingerprintManagerCompat.CryptoObject

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
        cancellationSignal = FingerprintManagerCancellationSignal()

        // As soon as this is called, we are listening for fingerprint introduction.
        fingerprintHardware.authenticate(cryptoObject, 0, cancellationSignal.cancellationSignal, this, null)
    }

    fun setFingerprintHardware(hardware: FingerprintHardware, cryptoObject: FingerprintManagerCompat.CryptoObject) {
        fingerprintHardware = hardware
        this.cryptoObject = cryptoObject
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        if (cancellationSignal.isCancelled.not()) {
            onAuthenticationFailed()
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        close()
        view.onAuthenticationFailedWithHelp(helpString.toString())
    }

    override fun onAuthenticationFailed() {
        close()
        view.onFingerprintNotRecognized()
    }

    fun onDialogCancelled() =
            view.onCancelled()

    interface View : KFingerprintManager.FingerprintBaseCallback {
        fun onFingerprintDisplayed()

        fun close()
    }
}
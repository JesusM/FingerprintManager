package com.jesusm.jfingerprintmanager.encryption.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.jfingerprintmanager.base.ui.presenter.FingerprintBaseDialogPresenter

class FingerprintEncryptionDialogPresenter(view : View) : FingerprintBaseDialogPresenter(view) {
    interface View : FingerprintBaseDialogPresenter.View {
        fun onAuthenticationSucceed(cryptoObject: FingerprintManagerCompat.CryptoObject)
    }

    override fun onViewShown() {
        fingerprintHardware?.apply {
            if (isFingerprintAuthAvailable().not()) {
                close()
                return
            }
        }

        super.onViewShown()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)

        result?.let {
            (view as View).onAuthenticationSucceed(it.cryptoObject)
        }
        close()
    }
}
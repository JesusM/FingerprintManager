package com.jesusm.jfingerprintmanager.encryption.ui

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.jfingerprintmanager.JFingerprintManager
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment
import com.jesusm.jfingerprintmanager.encryption.presenter.FingerprintEncryptionDialogPresenter

class FingerprintEncryptionDialogFragment : FingerprintBaseDialogFragment<FingerprintEncryptionDialogPresenter>(), FingerprintEncryptionDialogPresenter.View {

    override fun onAuthenticationSucceed(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        (callback as JFingerprintManager.EncryptionAuthenticatedCallback).onAuthenticationSuccess(cryptoObject)
    }

    class Builder : FingerprintBaseDialogFragment.Builder<FingerprintEncryptionDialogFragment, FingerprintEncryptionDialogPresenter>() {
        override fun addProperties(dialogFragment: FingerprintEncryptionDialogFragment) {

        }

        override fun createDialogFragment(): FingerprintEncryptionDialogFragment {
            return FingerprintEncryptionDialogFragment()
        }

        override fun createPresenter(view: FingerprintEncryptionDialogFragment): FingerprintEncryptionDialogPresenter {
            return FingerprintEncryptionDialogPresenter(view)
        }
    }
}
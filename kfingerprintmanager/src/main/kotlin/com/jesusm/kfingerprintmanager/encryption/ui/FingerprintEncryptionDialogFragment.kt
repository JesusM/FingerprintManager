package com.jesusm.kfingerprintmanager.encryption.ui

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.ui.FingerprintBaseDialogFragment
import com.jesusm.kfingerprintmanager.encryption.presenter.FingerprintEncryptionDialogPresenter

class FingerprintEncryptionDialogFragment : FingerprintBaseDialogFragment<FingerprintEncryptionDialogPresenter>(), FingerprintEncryptionDialogPresenter.View {

    override fun onAuthenticationSucceed(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        (callback as KFingerprintManager.EncryptionAuthenticatedCallback).onAuthenticationSuccess(cryptoObject)
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
package com.jesusm.kfingerprintmanager.base


import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.nhaarman.mockito_kotlin.mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as _when

open class BasePresenterTest {
    val mockView: FingerprintAuthenticationDialogPresenter.View = mock()

    val mockFingerprintHardware: FingerprintHardware = mock()

    fun createPresenter(view: FingerprintAuthenticationDialogPresenter.View): FingerprintAuthenticationDialogPresenter {
        return createPresenter(view, false, true)
    }

    fun createPresenter(view: FingerprintAuthenticationDialogPresenter.View,
                        newFingerprintEnrolled: Boolean, fingerprintAvailable: Boolean): FingerprintAuthenticationDialogPresenter {
        val presenter = FingerprintAuthenticationDialogPresenter(view)

        if (newFingerprintEnrolled) {
            presenter.newFingerprintEnrolled()
        }

        _when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(fingerprintAvailable)

        presenter.setFingerprintHardware(mockFingerprintHardware, mock<FingerprintManagerCompat.CryptoObject>(FingerprintManagerCompat.CryptoObject::class.java))

        return presenter
    }
}
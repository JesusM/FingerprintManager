package com.jesusm.kfingerprintmanager.authentication.presenter

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.base.BasePresenterTest
import com.jesusm.kfingerprintmanager.base.model.FingerprintManagerCancellationSignal
import com.jesusm.kfingerprintmanager.base.ui.presenter.FingerprintBaseDialogPresenter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.*

class FingerprintAuthenticationDialogPresenterTest : BasePresenterTest() {
    @Test
    fun shouldUsePasswordIfFingerprintNotAvailable() {
        val presenter = createPresenter(mockView, false, false)

        presenter.onViewShown()

        assertGoToPasswordFlowIsCalled(mockView, presenter)
    }

    @Test
    fun shouldUsePasswordIfSelected() {
        val presenter = createPresenter(mockView, true, true)
        presenter.stage = FingerprintBaseDialogPresenter.Stage.FINGERPRINT

        presenter.showPasswordClicked()

        assertGoToPasswordFlowIsCalled(mockView, presenter)
    }

    @Test
    fun shouldStopFingerprintListenerWhenShowingPassword() {
        val presenter = createPresenter(mockView, true, true)
        presenter.stage = FingerprintBaseDialogPresenter.Stage.FINGERPRINT

        val mockCancellationSignal = mock<FingerprintManagerCancellationSignal>(FingerprintManagerCancellationSignal::class.java)
        presenter.cancellationSignal = mockCancellationSignal

        presenter.showPasswordClicked()

        verify<FingerprintManagerCancellationSignal>(mockCancellationSignal).cancel()
        verify(mockView, never()).onAuthenticationFailedWithHelp(anyString())
    }

    private fun assertGoToPasswordFlowIsCalled(view: FingerprintAuthenticationDialogPresenter.View,
                                               presenter: FingerprintAuthenticationDialogPresenter) {
        verify<FingerprintAuthenticationDialogPresenter.View>(view).onPasswordViewDisplayed(false)
        assertStageState(presenter, FingerprintBaseDialogPresenter.Stage.PASSWORD)
    }

    @Test
    fun shouldShowFingerprintIfAvailable() {
        val presenter = createPresenter(mockView, false, true)

        presenter.onViewShown()

        verify(mockView).onFingerprintDisplayed()
        verify(mockView, never()).onPasswordViewDisplayed(anyBoolean())
        assertStageState(presenter, FingerprintBaseDialogPresenter.Stage.FINGERPRINT)
    }

    @Test
    fun shouldShowWarningMessageIfEmptyPasswordEntered() {
        val presenter = createPresenter(mockView, false, true)
        presenter.stage = FingerprintBaseDialogPresenter.Stage.PASSWORD

        presenter.onPasswordEntered("", false)

        verify(mockView).onPasswordEmpty()
    }

    @Test
    fun shouldShowPasswordIfNewFingerprintEnrolled() {
        val presenter = createPresenter(mockView, true, true)

        presenter.onViewShown()

        verify(mockView).onPasswordViewDisplayed(true)
    }

    @Test
    fun shouldSaveUseFingerprintInFuture() {
        val presenter = createPresenter(mockView, true, true)
        presenter.onViewShown()

        presenter.onPasswordEntered("password", true)

        verify(mockView).saveUseFingerprintFuture(true)
    }

    @Test
    fun shouldCreateKeyWhenEnteringPasswordAndNewFingerprintEnrolled() {
        val presenter = createPresenter(mockView, true, true)
        presenter.onViewShown()

        presenter.onPasswordEntered("password", true)

        verify(mockView).createKey()
        assertStageState(presenter, FingerprintBaseDialogPresenter.Stage.FINGERPRINT)
    }

    @Test
    fun shouldReturnPasswordAfterPasswordCorrectlyEntered() {
        val presenter = createPresenter(mockView, false, false)
        presenter.onViewShown()

        val passwordEntered = "password"
        presenter.onPasswordEntered(passwordEntered, false)

        verify(mockView).onPasswordInserted(passwordEntered)
        verify(mockView).close()
    }

    @Test
    fun shouldCloseWhenFingerprintCorrectlyEntered() {
        val presenter = createPresenter(mockView, false, true)
        presenter.onViewShown()
        val mock = mock<FingerprintManagerCompat.CryptoObject>(FingerprintManagerCompat.CryptoObject::class.java)
        presenter.onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult(mock))

        verify(mockView).onAuthenticationSucceed()
        verify(mockView).close()
    }

    private fun assertStageState(presenter: FingerprintAuthenticationDialogPresenter,
                                 expected: FingerprintBaseDialogPresenter.Stage) {
        assertEquals(expected, presenter.stage)
    }
}
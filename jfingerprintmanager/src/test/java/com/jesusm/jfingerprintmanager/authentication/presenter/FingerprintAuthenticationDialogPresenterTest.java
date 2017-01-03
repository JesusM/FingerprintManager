package com.jesusm.jfingerprintmanager.authentication.presenter;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.base.BasePresenterTest;
import com.jesusm.jfingerprintmanager.base.model.FingerprintManagerCancellationSignal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FingerprintAuthenticationDialogPresenterTest extends BasePresenterTest {

    @Test
    public void shouldUsePasswordIfFingerprintNotAvailable() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, false, false);

        presenter.onViewShown();

        assertGoToPasswordFlowIsCalled(mockView, presenter);
    }

    @Test
    public void shouldUsePasswordIfSelected() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, true, true);
        presenter.setStage(FingerprintAuthenticationDialogPresenter.BaseStage.FINGERPRINT);

        presenter.showPasswordClicked();

        assertGoToPasswordFlowIsCalled(mockView, presenter);
    }

    @Test
    public void shouldStopFingerprintListenerWhenShowingPassword() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, true, true);
        presenter.setStage(FingerprintAuthenticationDialogPresenter.BaseStage.FINGERPRINT);

        FingerprintManagerCancellationSignal mockCancellationSignal = mock(FingerprintManagerCancellationSignal.class);
        presenter.setCancellationSignal(mockCancellationSignal);

        presenter.showPasswordClicked();

        verify(mockCancellationSignal).cancel();
        verify(mockView, never()).onAuthenticationFailedWithHelp(anyString());
    }

    private void assertGoToPasswordFlowIsCalled(FingerprintAuthenticationDialogPresenter.View view,
                                                FingerprintAuthenticationDialogPresenter presenter) {
        verify(view).onPasswordViewDisplayed(false);
        assertStageState(presenter, FingerprintAuthenticationDialogPresenter.AuthenticationStage.PASSWORD);
    }

    @Test
    public void shouldShowFingerprintIfAvailable() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, false, true);

        presenter.onViewShown();

        verify(mockView).onFingerprintDisplayed();
        verify(mockView, never()).onPasswordViewDisplayed(anyBoolean());
        assertStageState(presenter, FingerprintAuthenticationDialogPresenter.BaseStage.FINGERPRINT);
    }

    @Test
    public void shouldShowWarningMessageIfEmptyPasswordEntered() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, false, true);
        presenter.setStage(FingerprintAuthenticationDialogPresenter.AuthenticationStage.PASSWORD);

        presenter.onPasswordEntered("", false);

        verify(mockView).onPasswordEmpty();
    }

    @Test
    public void shouldShowPasswordIfNewFingerprintEnrolled() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, true, true);

        presenter.onViewShown();

        verify(mockView).onPasswordViewDisplayed(true);
    }

    @Test
    public void shouldSaveUseFingerprintInFuture() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, true, true);
        presenter.onViewShown();

        presenter.onPasswordEntered("password", true);

        verify(mockView).saveUseFingerprintFuture(true);
    }

    @Test
    public void shouldCreateKeyWhenEnteringPasswordAndNewFingerprintEnrolled() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, true, true);
        presenter.onViewShown();

        presenter.onPasswordEntered("password", true);

        verify(mockView).createKey();
        assertStageState(presenter, FingerprintAuthenticationDialogPresenter.BaseStage.FINGERPRINT);
    }

    @Test
    public void shouldReturnPasswordAfterPasswordCorrectlyEntered() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, false, false);
        presenter.onViewShown();

        String passwordEntered = "password";
        presenter.onPasswordEntered(passwordEntered, false);

        verify(mockView).onPasswordInserted(passwordEntered);
        verify(mockView).close();
    }

    @Test
    public void shouldCloseWhenFingerprintCorrectlyEntered() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView, false, true);
        presenter.onViewShown();
        FingerprintManagerCompat.CryptoObject mock = mock(FingerprintManagerCompat.CryptoObject.class);
        presenter.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(mock));

        verify(mockView).onAuthenticationSucceed();
        verify(mockView).close();
    }

    private void assertStageState(FingerprintAuthenticationDialogPresenter presenter,
                                  FingerprintAuthenticationDialogPresenter.Stage expected) {
        assertEquals(expected, presenter.getStage());
    }
}

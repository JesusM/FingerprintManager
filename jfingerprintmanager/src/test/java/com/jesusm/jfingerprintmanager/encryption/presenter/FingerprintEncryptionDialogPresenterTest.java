package com.jesusm.jfingerprintmanager.encryption.presenter;

import com.jesusm.jfingerprintmanager.base.BasePresenterTest;
import com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FingerprintEncryptionDialogPresenterTest extends BasePresenterTest {

    @Test
    public void shouldCloseIfFingerprintNotAvailable() {
        FingerprintAuthenticationDialogPresenter presenter = createPresenter(mockView);
        presenter.onViewShown();

        verify(mockView).onFingerprintDisplayed();
    }
}

package com.jesusm.jfingerprintmanager.base;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasePresenterTest {

    @Mock
    protected FingerprintAuthenticationDialogPresenter.View mockView;
    @Mock
    private FingerprintHardware mockFingerprintHardware;

    protected FingerprintAuthenticationDialogPresenter createPresenter(FingerprintAuthenticationDialogPresenter.View view) {
        return createPresenter(view, false, true);
    }

    protected FingerprintAuthenticationDialogPresenter createPresenter(FingerprintAuthenticationDialogPresenter.View view,
                                                                     boolean newFingerprintEnrolled, boolean fingerprintAvailable) {
        FingerprintAuthenticationDialogPresenter presenter = new FingerprintAuthenticationDialogPresenter(view);

        if (newFingerprintEnrolled) {
            presenter.newFingerprintEnrolled();
        }

        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(fingerprintAvailable);
        presenter.setFingerprintHardware(mockFingerprintHardware, mock(FingerprintManagerCompat.CryptoObject.class));

        return presenter;
    }
}

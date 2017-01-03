package com.jesusm.jfingerprintmanager.base.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.jesusm.jfingerprintmanager.utils.CompatUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FingerprintHardwareTest {

    @Mock
    Context mockContext;
    @Mock
    CompatUtils mockCompatUtils;

    @Test
    public void notAvailableIfHardwareNotPresent() {
        FingerprintHardware.FingerprintManager mockFingerprintManager = Mockito.mock(FingerprintHardware.FingerprintManager.class);
        Mockito.when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        FingerprintHardware fingerprintHardware = createFingerprintHardware(mockFingerprintManager);

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable());
    }

    @Test
    public void notAvailableIfFingerprintNotEnrolled() {
        FingerprintHardware.FingerprintManager mockFingerprintManager = Mockito.mock(FingerprintHardware.FingerprintManager.class);
        Mockito.when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);
        FingerprintHardware fingerprintHardware = createFingerprintHardware(mockFingerprintManager);

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable());
    }

    @Test
    public void notAvailableIfPermissionNotGranted() {
        FingerprintHardware.FingerprintManager mockFingerprintManager = Mockito.mock(FingerprintHardware.FingerprintManager.class);
        Mockito.when(mockFingerprintManager.isPermissionGranted()).thenReturn(false);
        FingerprintHardware fingerprintHardware = createFingerprintHardware(mockFingerprintManager);

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable());
    }

    @Test
    public void notAvailableIfPreMarshMallow() {
        Mockito.when(mockCompatUtils.isMarshmallow()).thenReturn(false);
        FingerprintHardware fingerprintHardware = createFingerprintHardware();

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable());
    }

    @Test
    public void availableIfConditionsOK() {
        Mockito.when(mockCompatUtils.isMarshmallow()).thenReturn(true);
        FingerprintHardware fingerprintHardware = createFingerprintHardware();

        Assert.assertTrue(fingerprintHardware.isFingerprintAuthAvailable());
    }

    private FingerprintHardware createFingerprintHardware() {
        return createFingerprintHardware(new FakeFingerprintHardware());
    }

    @SuppressLint("VisibleForTests")
    private FingerprintHardware createFingerprintHardware(FingerprintHardware.FingerprintManager fingerprintManager) {
        return new FingerprintHardware(mockContext, fingerprintManager, mockCompatUtils);
    }

    private class FakeFingerprintHardware implements FingerprintHardware.FingerprintManager {

        @Override
        public void authenticate(@Nullable FingerprintManagerCompat.CryptoObject crypto, int flags,
                                 @Nullable CancellationSignal cancel,
                                 @NonNull FingerprintManagerCompat.AuthenticationCallback callback,
                                 @Nullable Handler handler) {
        }

        @Override
        public boolean hasEnrolledFingerprints() {
            return true;
        }

        @Override
        public boolean isHardwareDetected() {
            return true;
        }

        @Override
        public boolean isPermissionGranted() {
            return true;
        }
    }
}

package com.jesusm.jfingerprintmanager.base.hardware;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.jesusm.jfingerprintmanager.utils.CompatUtils;

public class FingerprintHardware {
    private final FingerprintManager fingerprintManager;
    private final Context context;
    private final CompatUtils compatUtils;

    public FingerprintHardware(Context context) {
        this.context = context;
        this.fingerprintManager = new SystemFingerprintManager(context);
        this.compatUtils = new CompatUtils();
    }

    @VisibleForTesting
    FingerprintHardware(Context context, FingerprintManager systemFingerprintManager,
                        CompatUtils compatUtils) {
        this.context = context;
        this.fingerprintManager = systemFingerprintManager;
        this.compatUtils = compatUtils;
    }

    public boolean isFingerprintAuthAvailable() {
        if (fingerprintManager == null)
            return false;

        if (!compatUtils.isMarshmallow()) {
            return false;
        }

        return fingerprintManager.isHardwareDetected() && hasFingerprintRegistered();
    }

    @SuppressWarnings("MissingPermission")
    private boolean hasFingerprintRegistered() {
        return isPermissionGranted() && hasEnrolledFingerprints();
    }

    private boolean hasEnrolledFingerprints() {
        return fingerprintManager.hasEnrolledFingerprints();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted() {
        return fingerprintManager.isPermissionGranted();
    }

    public void authenticate(FingerprintManagerCompat.CryptoObject cryptoObject, int flags,
                             CancellationSignal cancellationSignal,
                             FingerprintManagerCompat.AuthenticationCallback authenticationCallback,
                             Handler handler) {
        fingerprintManager.authenticate(cryptoObject, flags, cancellationSignal, authenticationCallback, handler);
    }

    interface FingerprintManager {
        void authenticate(@Nullable FingerprintManagerCompat.CryptoObject crypto, int flags,
                          @Nullable CancellationSignal cancel,
                          @NonNull FingerprintManagerCompat.AuthenticationCallback callback,
                          @Nullable Handler handler);

        boolean hasEnrolledFingerprints();

        boolean isHardwareDetected();

        boolean isPermissionGranted();
    }

    private class SystemFingerprintManager implements FingerprintManager {

        private final FingerprintManagerCompat fingerprintManagerCompat;

        SystemFingerprintManager(Context context) {
            fingerprintManagerCompat = FingerprintManagerCompat.from(context);
        }

        @Override
        public void authenticate(@Nullable FingerprintManagerCompat.CryptoObject crypto, int flags,
                                 @Nullable CancellationSignal cancel,
                                 @NonNull FingerprintManagerCompat.AuthenticationCallback callback,
                                 @Nullable Handler handler) {
            fingerprintManagerCompat.authenticate(crypto, flags, cancel, callback, handler);
        }

        @Override
        public boolean hasEnrolledFingerprints() {
            return fingerprintManagerCompat.hasEnrolledFingerprints();
        }

        @Override
        public boolean isHardwareDetected() {
            return fingerprintManagerCompat.isHardwareDetected();
        }

        @TargetApi(Build.VERSION_CODES.M)
        public boolean isPermissionGranted() {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
}

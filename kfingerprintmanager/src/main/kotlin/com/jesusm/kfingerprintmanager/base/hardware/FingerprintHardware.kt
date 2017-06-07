package com.jesusm.kfingerprintmanager.base.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import com.jesusm.kfingerprintmanager.utils.CompatUtils

class FingerprintHardware(val context: Context,
                          val fingerprintManager: FingerprintManager = FingerprintHardware.SystemFingerprintManager(context),
                          val compatUtils: CompatUtils = CompatUtils()) {

    fun isFingerprintAuthAvailable() = compatUtils.isMarshmallow() && fingerprintManager.isHardwareDetected && hasFingerprintRegistered()

    private fun hasFingerprintRegistered(): Boolean = isPermissionGranted() && fingerprintManager.hasEnrolledFingerprints()

    private fun isPermissionGranted(): Boolean = fingerprintManager.isPermissionGranted

    fun authenticate(crypto: FingerprintManagerCompat.CryptoObject, flags: Int, cancellationSignal: CancellationSignal,
                     callback: FingerprintManagerCompat.AuthenticationCallback, handler: Handler?) {
        fingerprintManager.authenticate(crypto, flags, cancellationSignal, callback, handler)
    }

    interface FingerprintManager {
        fun authenticate(crypto: FingerprintManagerCompat.CryptoObject?, flags: Int,
                         cancel: CancellationSignal?,
                         callback: FingerprintManagerCompat.AuthenticationCallback,
                         handler: Handler?)

        fun hasEnrolledFingerprints(): Boolean

        val isHardwareDetected: Boolean

        val isPermissionGranted: Boolean
    }

    private class SystemFingerprintManager(val context: Context,
                                           private val fingerprintManagerCompat: FingerprintManagerCompat = FingerprintManagerCompat.from(context)) : FingerprintManager {

        override fun authenticate(crypto: FingerprintManagerCompat.CryptoObject?, flags: Int,
                                  cancel: CancellationSignal?,
                                  callback: FingerprintManagerCompat.AuthenticationCallback,
                                  handler: Handler?) {
            fingerprintManagerCompat.authenticate(crypto, flags, cancel, callback, handler)
        }

        override fun hasEnrolledFingerprints(): Boolean {
            return fingerprintManagerCompat.hasEnrolledFingerprints()
        }

        override val isHardwareDetected: Boolean = fingerprintManagerCompat.isHardwareDetected

        override val isPermissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
    }
}
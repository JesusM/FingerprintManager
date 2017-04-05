package com.jesusm.jfingerprintmanager.base.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import com.jesusm.jfingerprintmanager.utils.CompatUtils
import com.nhaarman.mockito_kotlin.mock
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as _when

class FingerprintHardwareTest {
    val mockContext: Context = mock()
    val mockCompatUtils: CompatUtils = mock()
    val mockFingerprintManager : FingerprintHardware.FingerprintManager = mock()

    @Test
    fun notAvailableIfHardwareNotPresent() {
        _when(mockFingerprintManager.isHardwareDetected).thenReturn(false)
        val fingerprintHardware = createFingerprintHardware(mockFingerprintManager)

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable())
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun notAvailableIfFingerprintNotEnrolled() {
        _when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false)
        val fingerprintHardware = createFingerprintHardware(mockFingerprintManager)

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable())
    }

    @Test
    fun notAvailableIfPermissionNotGranted() {
        val mockFingerprintManager = Mockito.mock(FingerprintHardware.FingerprintManager::class.java)
        _when(mockFingerprintManager.isPermissionGranted).thenReturn(false)
        val fingerprintHardware = createFingerprintHardware(mockFingerprintManager)

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable())
    }

    @Test
    fun notAvailableIfPreMarshMallow() {
        _when(mockCompatUtils.isMarshmallow()).thenReturn(false)
        val fingerprintHardware = createFingerprintHardware()

        Assert.assertFalse(fingerprintHardware.isFingerprintAuthAvailable())
    }

    @Test
    fun availableIfConditionsOK() {
        _when(mockCompatUtils.isMarshmallow()).thenReturn(true)
        val fingerprintHardware = createFingerprintHardware()

        Assert.assertTrue(fingerprintHardware.isFingerprintAuthAvailable())
    }

    private fun createFingerprintHardware(): FingerprintHardware {
        return createFingerprintHardware(FakeFingerprintHardware())
    }

    @SuppressLint("VisibleForTests")
    private fun createFingerprintHardware(fingerprintManager: FingerprintHardware.FingerprintManager): FingerprintHardware {
        return FingerprintHardware(mockContext, fingerprintManager, mockCompatUtils)
    }

    private inner class FakeFingerprintHardware : FingerprintHardware.FingerprintManager {

        override fun authenticate(crypto: FingerprintManagerCompat.CryptoObject?, flags: Int,
                                  cancel: CancellationSignal?,
                                  callback: FingerprintManagerCompat.AuthenticationCallback,
                                  handler: Handler?) {
        }

        override fun hasEnrolledFingerprints(): Boolean {
            return true
        }

        override val isHardwareDetected: Boolean
            get() = true

        override val isPermissionGranted: Boolean
            get() = true
    }
}
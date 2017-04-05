package com.jesusm.jfingerprintmanager.authentication

import com.jesusm.jfingerprintmanager.BaseTest
import com.jesusm.jfingerprintmanager.JFingerprintManager
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager
import com.nhaarman.mockito_kotlin.doThrow
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import org.mockito.Mockito.`when` as _when

class AuthenticationManagerTest : BaseTest() {
    @Test
    fun notAvailableIfHardwareNotPresent() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false)

        val authenticationCallback = Mockito.mock<JFingerprintManager.AuthenticationCallback>(JFingerprintManager.AuthenticationCallback::class.java)

        jFingerprintManager.authenticate(authenticationCallback, mockFragmentManager)
        verify<JFingerprintManager.AuthenticationCallback>(authenticationCallback).onFingerprintNotAvailable()
    }

    @Test
    fun notAvailableIfFingerprintNotEnrolled() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(false)

        val authenticationCallback = Mockito.mock<JFingerprintManager.AuthenticationCallback>(JFingerprintManager.AuthenticationCallback::class.java)
        jFingerprintManager.authenticate(authenticationCallback, mockFragmentManager)

        verify<JFingerprintManager.AuthenticationCallback>(authenticationCallback).onFingerprintNotAvailable()
        verify<JFingerprintManager.AuthenticationCallback>(authenticationCallback, never()).onAuthenticationSuccess()
    }

    @Test
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, KeyStoreManager.InitialisationException::class, KeyStoreManager.NewFingerprintEnrolledException::class)
    fun notAvailableIfCipherCreationError() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when(mockKeyStoreManager.initDefaultCipher(KEY_STORE_ALIAS)).doThrow(NoSuchAlgorithmException())

        val authenticationCallback = Mockito.mock<JFingerprintManager.AuthenticationCallback>(JFingerprintManager.AuthenticationCallback::class.java)
        jFingerprintManager.authenticate(authenticationCallback, mockFragmentManager)

        verify<JFingerprintManager.AuthenticationCallback>(authenticationCallback).onFingerprintNotAvailable()
        verify<JFingerprintManager.AuthenticationCallback>(authenticationCallback, never()).onAuthenticationSuccess()
    }

    @Test
    fun authenticationDisplayedIfCreationSuccessful() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(anyString())).thenReturn(Mockito.mock(Cipher::class.java))

        val authenticationCallback = Mockito.mock<JFingerprintManager.AuthenticationCallback>(JFingerprintManager.AuthenticationCallback::class.java)
        jFingerprintManager.authenticate(authenticationCallback, mockFragmentManager)

        verify<FakeSystem>(mockSystem).showDialog()
    }
}
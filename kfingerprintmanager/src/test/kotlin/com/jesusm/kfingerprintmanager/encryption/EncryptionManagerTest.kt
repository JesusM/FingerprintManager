package com.jesusm.kfingerprintmanager.encryption

import com.jesusm.kfingerprintmanager.BaseTest
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.mockito.Mockito
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import org.mockito.Mockito.`when` as _when

class EncryptionManagerTest : BaseTest() {

    @Test
    fun notAvailableIfHardwareNotPresent() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false)

        val encryptionCallback = Mockito.mock<KFingerprintManager.EncryptionCallback>(KFingerprintManager.EncryptionCallback::class.java)

        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager)
        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback).onFingerprintNotAvailable()
        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback, never()).onEncryptionSuccess(any())
    }

    @Test
    fun notAvailableIfFingerprintNotEnrolled() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(false)

        val encryptionCallback = Mockito.mock<KFingerprintManager.EncryptionCallback>(KFingerprintManager.EncryptionCallback::class.java)
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager)

        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback).onFingerprintNotAvailable()
        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback, never()).onEncryptionSuccess(any())
    }


    @Test
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    fun notAvailableIfCipherCreationError() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when(mockKeyStoreManager.createCipher()).doThrow(NoSuchAlgorithmException())

        val encryptionCallback = Mockito.mock<KFingerprintManager.EncryptionCallback>(KFingerprintManager.EncryptionCallback::class.java)
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager)

        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback).onFingerprintNotAvailable()
        verify<KFingerprintManager.EncryptionCallback>(encryptionCallback, never()).onEncryptionSuccess(any())
    }

    @Test
    fun fingerprintDisplayedIfCreationSuccessful() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(any())).thenReturn(mock<Cipher>())

        val encryptionCallback = Mockito.mock<KFingerprintManager.EncryptionCallback>(KFingerprintManager.EncryptionCallback::class.java)
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager)

        verify(mockSystem).showDialog()
    }

    @Test
    fun encryptionFailIfFingerprintNotPresent() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false)

        val callbackAdapter = Mockito.mock(EncryptionCallbackAdapter::class.java)
        val message = "message"
        jFingerprintManager.encrypt(message, callbackAdapter, mockFragmentManager)

        verify<EncryptionCallbackAdapter>(callbackAdapter).onFingerprintNotAvailable()
        verify<EncryptionCallbackAdapter>(callbackAdapter, never()).onEncryptionSuccess(any())
    }

    @Test
    fun encryptionFailIfMessageEmpty() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(any())).thenReturn(mock<Cipher>())

        val callbackAdapter = Mockito.mock(EncryptionCallbackAdapter::class.java)
        jFingerprintManager.encrypt("", callbackAdapter, mockFragmentManager)

        verify<EncryptionCallbackAdapter>(callbackAdapter).onEncryptionFailed()
        verify(mockSystem, never()).showDialog()
    }

    @Test
    fun encryptionDisplayedIfCreationSuccessful() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(any())).thenReturn(mock<Cipher>())

        val encryptionCallback = Mockito.mock<KFingerprintManager.EncryptionCallback>(KFingerprintManager.EncryptionCallback::class.java)
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager)

        verify(mockSystem).showDialog()
    }

    @Test
    fun decryptionFailIfMessageEmpty() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(any())).thenReturn(mock<Cipher>())

        val callbackAdapter = Mockito.mock<KFingerprintManager.DecryptionCallback>(KFingerprintManager.DecryptionCallback::class.java)
        jFingerprintManager.decrypt("", callbackAdapter, mockFragmentManager)

        verify<KFingerprintManager.DecryptionCallback>(callbackAdapter).onDecryptionFailed()
        verify(mockSystem, never()).showDialog()
    }

    @Test
    fun decryptionFailIfMessageStructureNotCorrect() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initDefaultCipher(any())).thenReturn(mock<Cipher>())

        val callbackAdapter = Mockito.mock<KFingerprintManager.DecryptionCallback>(KFingerprintManager.DecryptionCallback::class.java)
        jFingerprintManager.decrypt("message", callbackAdapter, mockFragmentManager)

        verify<KFingerprintManager.DecryptionCallback>(callbackAdapter).onDecryptionFailed()
        verify(mockSystem, never()).showDialog()
    }

    @Test
    fun decryptionDisplayedIfCreationSuccessful() {
        val jFingerprintManager = createFingerPrintManager()
        _when<Boolean>(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true)
        _when<Boolean>(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true)
        _when<Cipher>(mockKeyStoreManager.initCipherForDecryption(any(), com.nhaarman.mockito_kotlin.any())).thenReturn(mock<Cipher>())

        val callbackAdapter = Mockito.mock<KFingerprintManager.DecryptionCallback>(KFingerprintManager.DecryptionCallback::class.java)
        jFingerprintManager.decrypt("message:ivs", callbackAdapter, mockFragmentManager)

        verify(mockSystem).showDialog()
    }


    private inner class EncryptionCallbackAdapter : KFingerprintManager.EncryptionCallback {

        override fun onFingerprintNotRecognized() {
        }

        override fun onAuthenticationFailedWithHelp(help: String?) {
        }

        override fun onFingerprintNotAvailable() {
        }

        override fun onEncryptionSuccess(messageEncrypted: String) {
        }

        override fun onEncryptionFailed() {
        }

        override fun onCancelled() {
        }
    }
}
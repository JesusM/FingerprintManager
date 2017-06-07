package com.jesusm.kfingerprintmanager.base

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.util.Log
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.kfingerprintmanager.base.keystore.KeyStoreManager
import com.jesusm.kfingerprintmanager.base.model.FingerprintErrorState
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException

class FingerprintAssetsManager(val context: Context, val keyStoreAlias: String,
                               val fingerprintHardware: FingerprintHardware = FingerprintHardware(context),
                               val keyStoreManager: KeyStoreManager = KeyStoreManager(context)) {
    private var cipher: Cipher? = null
    var errorState: FingerprintErrorState? = null

    fun initSecureDependencies(callback: KFingerprintManager.InitialisationCallback) {
        initSecureDependenciesForDecryption(callback, null)
    }

    fun initSecureDependenciesForDecryption(callback: KFingerprintManager.InitialisationCallback,
                                            IVs: ByteArray?) {
        initSecureDependenciesWithIVs(callback, IVs)
    }

    private fun initSecureDependenciesWithIVs(callback: KFingerprintManager.InitialisationCallback,
                                              IVs: ByteArray?) {
        if (!isFingerprintAuthAvailable()) {
            handleError(callback, FingerprintErrorState.FINGERPRINT_NOT_AVAILABLE)
            return
        }

        if (!keyStoreManager.isFingerprintEnrolled()) {
            handleError(callback, FingerprintErrorState.FINGERPRINT_NOT_ENROLLED)
            return
        }

        try {
            if (IVs == null) {
                cipher = keyStoreManager.initDefaultCipher(keyStoreAlias)
            } else {
                cipher = keyStoreManager.initCipherForDecryption(keyStoreAlias, IVs)
            }
        } catch (e: KeyStoreManager.NewFingerprintEnrolledException) {
            handleError(callback, FingerprintErrorState.NEW_FINGERPRINT_ENROLLED)
            return
        } catch (e: NoSuchPaddingException) {
            handleError(callback, FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR)
            return
        } catch (e: NoSuchAlgorithmException) {
            handleError(callback, FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR)
            return
        } catch (e: KeyStoreManager.InitialisationException) {
            handleError(callback, FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR)
            return
        }

        val isCipherAvailable = cipher != null

        if (isCipherAvailable) {
            callback.onInitialisationSuccessfullyCompleted()
        } else {
            handleError(callback, FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR)
        }
    }

    private fun handleError(callback: KFingerprintManager.InitialisationCallback,
                            errorState: FingerprintErrorState) {
        this.errorState = errorState
        logError(errorState.errorMessage)

        when (errorState) {
            FingerprintErrorState.FINGERPRINT_NOT_AVAILABLE -> callback.onFingerprintNotAvailable()
            FingerprintErrorState.FINGERPRINT_INITIALISATION_ERROR -> callback.onErrorFingerprintNotInitialised()
            FingerprintErrorState.LOCK_SCREEN_RESET_OR_DISABLED, FingerprintErrorState.FINGERPRINT_NOT_ENROLLED -> callback.onErrorFingerprintNotEnrolled()
            else -> callback.onErrorFingerprintNotInitialised()
        }
    }

    private fun isFingerprintAuthAvailable(): Boolean = fingerprintHardware.isFingerprintAuthAvailable()

    fun createKey(invalidatedByBiometricEnrollment: Boolean) {
        try {
            keyStoreManager.createKey(keyStoreAlias, invalidatedByBiometricEnrollment)
        } catch (e: KeyStoreManager.InitialisationException) {
            logError(e.message)
        }
    }

    private fun logError(@StringRes message: Int) {
        if (message != -1) {
            logError(context.getString(message))
        }
    }

    private fun logError(message: String?) {
        Log.e(javaClass.simpleName, message)
    }

    fun getCryptoObject(): FingerprintManagerCompat.CryptoObject {
        return FingerprintManagerCompat.CryptoObject(cipher)
    }
}
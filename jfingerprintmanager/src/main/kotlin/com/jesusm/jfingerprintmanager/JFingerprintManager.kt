package com.jesusm.jfingerprintmanager

import android.content.Context
import android.support.annotation.StyleRes
import android.support.v4.app.FragmentManager
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.jfingerprintmanager.authentication.AuthenticationManager
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.jfingerprintmanager.base.ui.System
import com.jesusm.jfingerprintmanager.base.ui.SystemImpl
import com.jesusm.jfingerprintmanager.encryption.Base64Encoder
import com.jesusm.jfingerprintmanager.encryption.Encoder
import com.jesusm.jfingerprintmanager.encryption.EncryptionManager

class JFingerprintManager(context: Context,
                          keyStoreAlias: String,
                          system: System = SystemImpl(),
                          fingerprintAssetsManager: FingerprintAssetsManager = FingerprintAssetsManager(context, keyStoreAlias),
                          encoder: Encoder = Base64Encoder(),
                          val authenticationManager: AuthenticationManager = AuthenticationManager(fingerprintAssetsManager, system),
                          val encryptionManager: EncryptionManager = EncryptionManager(encoder, fingerprintAssetsManager, system)) {

    fun setAuthenticationStyle(@StyleRes styleRes: Int): Unit {
        authenticationManager.authenticationDialogStyle = styleRes
        encryptionManager.authenticationDialogStyle = styleRes
    }

    fun encrypt(messageToEncrypt: String, callback: EncryptionCallback,
                fragmentManager: FragmentManager): Unit =
            encryptionManager.encrypt(messageToEncrypt, callback, fragmentManager)

    fun decrypt(messageToDecrypt: String, callback: DecryptionCallback,
                fragmentManager: FragmentManager): Unit =
            encryptionManager.decrypt(messageToDecrypt, callback, fragmentManager)

    fun authenticate(authenticationCallback: AuthenticationCallback,
                     fragmentManager: FragmentManager) =
            authenticationManager.startAuthentication(authenticationCallback, fragmentManager)

    interface FingerprintBaseCallback {
        fun onFingerprintNotRecognized()

        fun onAuthenticationFailedWithHelp(help: String?)

        fun onFingerprintNotAvailable()

        fun onCancelled()
    }

    interface DecryptionCallback : FingerprintBaseCallback {
        fun onDecryptionSuccess(messageDecrypted: String)

        fun onDecryptionFailed()
    }

    interface EncryptionCallback : FingerprintBaseCallback {
        fun onEncryptionSuccess(messageEncrypted: String)

        fun onEncryptionFailed()
    }

    interface AuthenticationCallback : JFingerprintManager.FingerprintBaseCallback {
        fun onAuthenticationSuccess()

        fun onSuccessWithManualPassword(password: String)
    }

    interface EncryptionAuthenticatedCallback : FingerprintBaseCallback {
        fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject)
    }

    interface InitialisationCallback : FingerprintAvailabilityCallback {
        fun onErrorFingerprintNotInitialised()

        fun onErrorFingerprintNotEnrolled()

        fun onInitialisationSuccessfullyCompleted()
    }

    interface FingerprintAvailabilityCallback {
        fun onFingerprintNotAvailable()
    }
}
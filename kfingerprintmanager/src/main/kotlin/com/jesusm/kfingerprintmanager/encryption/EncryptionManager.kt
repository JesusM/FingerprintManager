package com.jesusm.kfingerprintmanager.encryption

import android.support.v4.app.FragmentManager
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.BaseFingerprintManager
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.ui.System
import com.jesusm.kfingerprintmanager.encryption.ui.FingerprintEncryptionDialogFragment
import java.io.UnsupportedEncodingException
import java.security.spec.InvalidParameterSpecException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec

class EncryptionManager(val encoder: Encoder, fingerprintAssetsManager: FingerprintAssetsManager, system: System) : BaseFingerprintManager(fingerprintAssetsManager, system) {

    fun encrypt(messageToEncrypt: String, encryptionCallback: KFingerprintManager.EncryptionCallback, fragmentManager: FragmentManager) {
        if (messageToEncrypt.isNullOrEmpty()) {
            encryptionCallback.onEncryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependencies(
                object : KFingerprintManager.InitialisationCallback {
                    override fun onErrorFingerprintNotInitialised() {
                        encryptionCallback.onEncryptionFailed()
                    }

                    override fun onErrorFingerprintNotEnrolled() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }

                    override fun onInitialisationSuccessfullyCompleted() {
                        val callback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                            override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                                val cipher = cryptoObject.cipher
                                try {
                                    val messageToEncryptBytes = cipher.doFinal(messageToEncrypt.toByteArray(charset("UTF-8")))
                                    val ivBytes = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv

                                    val encryptedMessage = EncryptionData(messageToEncryptBytes, ivBytes, encoder)
                                    encryptionCallback.onEncryptionSuccess(encryptedMessage.print())
                                } catch (e: UnsupportedEncodingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: InvalidParameterSpecException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: BadPaddingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: IllegalBlockSizeException) {
                                    encryptionCallback.onEncryptionFailed()
                                }

                            }

                            override fun onFingerprintNotRecognized() {
                                encryptionCallback.onFingerprintNotRecognized()
                            }

                            override fun onAuthenticationFailedWithHelp(help: String?) {
                                encryptionCallback.onAuthenticationFailedWithHelp(help)
                            }

                            override fun onFingerprintNotAvailable() {
                                encryptionCallback.onFingerprintNotAvailable()
                            }

                            override fun onCancelled() {
                                encryptionCallback.onCancelled()
                            }
                        }

                        val builder = FingerprintEncryptionDialogFragment.Builder()
                        showFingerprintDialog(builder, fragmentManager, callback)
                    }

                    override fun onFingerprintNotAvailable() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }
                })
    }

    fun decrypt(messageToDecrypt: String, callback: KFingerprintManager.DecryptionCallback, fragmentManager: FragmentManager) {
        if (messageToDecrypt.isNullOrEmpty()) {
            callback.onDecryptionFailed()
            return
        }

        val decryptionData = EncryptionData(messageToDecrypt, encoder)
        if (decryptionData.dataIsCorrect().not()) {
            callback.onDecryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependenciesForDecryption(object : KFingerprintManager.InitialisationCallback {
            override fun onErrorFingerprintNotInitialised() {
                callback.onDecryptionFailed()
            }

            override fun onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable()
            }

            override fun onInitialisationSuccessfullyCompleted() {
                val decryptionCallback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                    override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                        try {
                            val cipher = cryptoObject.cipher

                            val encryptedMessage = decryptionData.decodedMessage()
                            val decryptedMessageBytes = cipher.doFinal(encryptedMessage)
                            val decryptedMessage = String(decryptedMessageBytes)

                            callback.onDecryptionSuccess(decryptedMessage)
                        } catch (e: IllegalBlockSizeException) {
                            callback.onDecryptionFailed()
                        } catch (e: BadPaddingException) {
                            callback.onDecryptionFailed()
                        }
                    }

                    override fun onFingerprintNotRecognized() {
                        callback.onFingerprintNotRecognized()
                    }

                    override fun onAuthenticationFailedWithHelp(help: String?) {
                        callback.onAuthenticationFailedWithHelp(help)
                    }

                    override fun onFingerprintNotAvailable() {
                        callback.onFingerprintNotAvailable()
                    }

                    override fun onCancelled() {
                        callback.onCancelled()
                    }
                }

                val builder = FingerprintEncryptionDialogFragment.Builder()
                showFingerprintDialog(builder, fragmentManager, decryptionCallback)
            }

            override fun onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable()
            }
        }, decryptionData.decodedIVs())
    }
}

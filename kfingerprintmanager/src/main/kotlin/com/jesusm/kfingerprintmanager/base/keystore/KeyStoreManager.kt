package com.jesusm.kfingerprintmanager.base.keystore

import android.app.KeyguardManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import com.jesusm.kfingerprintmanager.utils.CompatUtils
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class KeyStoreManager(val context: Context,
                      private val ANDROID_KEY_STORE: String = "AndroidKeyStore",
                      private val compatUtils: CompatUtils = CompatUtils()) {

    private val keyguardManager: KeyguardManager by lazy {
        context.getSystemService(KeyguardManager::class.java)
    }

    fun isFingerprintEnrolled(): Boolean = keyguardManager.isKeyguardSecure

    @Throws(NewFingerprintEnrolledException::class, InitialisationException::class,
            NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    fun initDefaultCipher(key: String): Cipher {
        val defaultCipher = createCipher()
        initCipher(defaultCipher, key)
        return defaultCipher
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class)
    fun createCipher(): Cipher {
        val transformation = KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7
        return Cipher.getInstance(transformation)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun createKeyGenerator(): KeyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

    /**
     * Initialize the [Cipher] instance with the created key in the
     * [.createKey] method.

     * @param alias the key alias to init the cipher
     */
    @Throws(NewFingerprintEnrolledException::class, InitialisationException::class)
    private fun initCipher(cipher: Cipher?, alias: String) {
        if (cipher == null) {
            throw InitialisationException("Error initialising cipher for decryption", Exception())
        }

        try {
            val key = getKey(alias)
            cipher.init(Cipher.ENCRYPT_MODE, key)
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Lock screen has been disabled or reset after the key was generated, or fingerprint
            // got enrolled after the key was generated (while app is opened).
            //
            throw NewFingerprintEnrolledException("New fingerprint enrolled", e)
        } catch (e: InvalidKeyException) {
            throw InitialisationException("Error initialising cipher for decryption", e)
        }
    }

    private fun getKey(alias: String): SecretKey? {
        try {
            if (existsKey(alias)) {
                try {
                    val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                    keyStore.load(null)
                    return keyStore.getKey(alias, null) as SecretKey
                } catch (e: KeyStoreException) {
                    return null
                } catch (e: UnrecoverableKeyException) {
                    return null
                } catch (e: NoSuchAlgorithmException) {
                    return null
                } catch (e: CertificateException) {
                    return null
                } catch (e: IOException) {
                    return null
                }

            } else {
                createKey(alias, true)
                return getKey(alias)
            }
        } catch (e: CertificateException) {
            logError(e.message)
            return null
        } catch (e: IOException) {
            logError(e.message)
            return null
        } catch (e: KeyStoreException) {
            logError(e.message)
            return null
        } catch (e: NoSuchAlgorithmException) {
            logError(e.message)
            return null
        } catch (e: InitialisationException) {
            logError(e.message)
            return null
        }
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun existsKey(keyName: String): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val aliases = keyStore.aliases()

        while (aliases.hasMoreElements()) {
            if (keyName == aliases.nextElement()) {
                return true
            }
        }

        return false
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.

     * @param keyStoreAlias                    the name of the key to be created
     * *
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not
     * *                                         be invalidated even if a new fingerprint is enrolled.
     * *                                         The default value is `true`, so passing
     * *                                         `true` doesn't change the behavior
     * *                                         (the key will be invalidated if a new fingerprint is
     * *                                         enrolled.). Note that this parameter is only valid if
     * *                                         the app works on Android N developer preview.
     */
    @Throws(InitialisationException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun createKey(keyStoreAlias: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            val builder = KeyGenParameterSpec.Builder(keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to startAuthenticationListener with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (compatUtils.isN()) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }

            val keyGenerator = createKeyGenerator()

            keyGenerator.apply {
                init(builder.build())
                generateKey()
            }
        } catch (e: InvalidAlgorithmParameterException) {
            logError(e.message)
            throw InitialisationException(e.message, e)
        }
    }

    @Throws(InitialisationException::class, NewFingerprintEnrolledException::class)
    fun initCipherForDecryption(alias: String, iv: ByteArray): Cipher {
        try {
            val cipher = createCipher()
            val key = getKey(alias)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            return cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Lock screen has been disabled or reset after the key was generated, or fingerprint
            // got enrolled after the key was generated (while app is opened).
            //
            throw NewFingerprintEnrolledException("New fingerprint enrolled", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw InitialisationException("Error initialising cipher for decryption", e)
        } catch (e: InvalidKeyException) {
            throw InitialisationException("Error initialising cipher for decryption", e)
        } catch (e: NoSuchAlgorithmException) {
            throw InitialisationException("Error initialising cipher for decryption", e)
        } catch (e: NoSuchPaddingException) {
            throw InitialisationException("Error initialising cipher for decryption", e)
        }

    }

    private fun logError(message: String?) =  Log.e(javaClass.simpleName, message)

    inner class NewFingerprintEnrolledException internal constructor(message: String?, cause: KeyPermanentlyInvalidatedException) : Throwable(message, cause)

    class InitialisationException(message: String?, cause: Exception) : Throwable(message, cause)
}
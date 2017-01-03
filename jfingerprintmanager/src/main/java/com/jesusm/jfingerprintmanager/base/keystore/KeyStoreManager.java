package com.jesusm.jfingerprintmanager.base.keystore;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class KeyStoreManager {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private KeyguardManager keyguardManager;
    private Cipher defaultCipher;
    private Cipher cipherNotInvalidated;
    private String keyStoreAlias;

    public KeyStoreManager(Context context) {
        keyguardManager = context.getSystemService(KeyguardManager.class);
    }

    public void createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        String transformation = KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7;
        defaultCipher = Cipher.getInstance(transformation);
        cipherNotInvalidated = Cipher.getInstance(transformation);
    }

    public void createKeyGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
    }

    public void createKeyStore() throws KeyStoreException {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
    }

    public boolean isFingerprintEnrolled() {
        return keyguardManager.isKeyguardSecure();
    }

    private void logError(String message) {
        Log.e(getClass().getSimpleName(), message);
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyStoreAlias                    the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    public void createKey(@NonNull String keyStoreAlias, boolean invalidatedByBiometricEnrollment)
            throws RuntimeException {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            this.keyStoreAlias = keyStoreAlias;
            keyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to startAuthenticationListener with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            logError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void initDefaultCipher() throws NewFingerprintEnrolledException, RuntimeException {
        try {
            initCipher(defaultCipher, this.keyStoreAlias);
        } catch (KeyStoreException |
                UnrecoverableKeyException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     */
    private void initCipher(Cipher cipher, String keyName) throws RuntimeException,
            NewFingerprintEnrolledException, InvalidKeyException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            // Lock screen has been disabled or reset after the key was generated, or fingerprint
            // got enrolled after the key was generated (while app is opened).
            //
            throw new NewFingerprintEnrolledException("New fingerprint enrolled", e);
        }
    }

    public class NewFingerprintEnrolledException extends Throwable {
        NewFingerprintEnrolledException(String message, KeyPermanentlyInvalidatedException cause) {
            super(message, cause);
        }
    }

    public Cipher getDefaultCipher() {
        return defaultCipher;
    }

    public boolean isCipherAvailable() {
        return defaultCipher != null;
    }

}

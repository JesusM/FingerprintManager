package com.jesusm.jfingerprintmanager.base.keystore;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeyStoreManager {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private KeyGenerator keyGenerator;
    private KeyguardManager keyguardManager;

    public KeyStoreManager(Context context) {
        keyguardManager = context.getSystemService(KeyguardManager.class);
    }

    public Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        String transformation = KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7;
        return Cipher.getInstance(transformation);
    }

    public void createKeyGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
    }

    public boolean isFingerprintEnrolled() {
        return keyguardManager.isKeyguardSecure();
    }

    private void logError(String message) {
        Log.e(getClass().getSimpleName(), message);
    }

    public Cipher initDefaultCipher(String key) throws NewFingerprintEnrolledException, InitialisationException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = createCipher();
        initCipher(cipher, key);
        return cipher;
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param alias the key alias to init the cipher
     */
    private void initCipher(Cipher cipher, String alias) throws NewFingerprintEnrolledException,
            InitialisationException {
        try {
            SecretKey key = getKey(alias);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            // Lock screen has been disabled or reset after the key was generated, or fingerprint
            // got enrolled after the key was generated (while app is opened).
            //
            throw new NewFingerprintEnrolledException("New fingerprint enrolled", e);
        } catch (InvalidKeyException e) {
            throw new InitialisationException("Error initialising cipher for decryption", e);
        }
    }

    public Cipher initCipherForDecryption(String alias, byte[] iv) throws InitialisationException,
            NewFingerprintEnrolledException {
        try {
            Cipher cipher = createCipher();
            SecretKey key = getKey(alias);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher;
        } catch (KeyPermanentlyInvalidatedException e) {
            // Lock screen has been disabled or reset after the key was generated, or fingerprint
            // got enrolled after the key was generated (while app is opened).
            //
            throw new NewFingerprintEnrolledException("New fingerprint enrolled", e);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new InitialisationException("Error initialising cipher for decryption", e);
        }
    }

    public class NewFingerprintEnrolledException extends Throwable {
        NewFingerprintEnrolledException(String message, KeyPermanentlyInvalidatedException cause) {
            super(message, cause);
        }
    }

    public static class InitialisationException extends Throwable {
        InitialisationException(String message, Exception cause) {
            super(message, cause);
        }
    }

    @Nullable
    private SecretKey getKey(String alias) {
        try {
            if (existsKey(alias)) {
                try {
                    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
                    keyStore.load(null);
                    return (SecretKey) keyStore.getKey(alias, null);
                } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                        CertificateException | IOException e) {
                    return null;
                }
            } else {
                createKey(alias, true);
                return getKey(alias);
            }
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException | InitialisationException e) {
            logError(e.getMessage());
            return null;
        }
    }

    private boolean existsKey(String keyName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            if (keyName.equals(aliases.nextElement())) {
                return true;
            }
        }

        return false;
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
            throws InitialisationException {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
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
        } catch (InvalidAlgorithmParameterException e) {
            logError(e.getMessage());
            throw new InitialisationException(e.getMessage(), e);
        }
    }
}

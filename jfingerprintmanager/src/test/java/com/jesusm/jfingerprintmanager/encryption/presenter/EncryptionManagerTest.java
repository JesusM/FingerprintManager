package com.jesusm.jfingerprintmanager.encryption.presenter;

import com.jesusm.jfingerprintmanager.BaseTest;
import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;
import com.jesusm.jfingerprintmanager.utils.TextUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionManagerTest extends BaseTest {

    @Test
    public void notAvailableIfHardwareNotPresent() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false);

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);

        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);
        verify(encryptionCallback).onFingerprintNotAvailable();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfFingerprintNotEnrolled() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(false);

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onFingerprintNotAvailable();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfKeyStoreCreationError() throws KeyStoreException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new KeyStoreException()).when(mockKeyStoreManager).createKeyStore();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfKeyGeneratorCreationError() throws NoSuchProviderException, NoSuchAlgorithmException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new NoSuchAlgorithmException()).when(mockKeyStoreManager).createKeyGenerator();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfCipherCreationError() throws NoSuchPaddingException, NoSuchAlgorithmException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new NoSuchAlgorithmException()).when(mockKeyStoreManager).createCipher();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfCipherInitialisationError() throws UnrecoverableKeyException,
            CertificateException, KeyStoreException, InvalidKeyException, IOException,
            NoSuchAlgorithmException, KeyStoreManager.NewFingerprintEnrolledException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).initDefaultCipher();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfKeyCreationError() throws RuntimeException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).createKey(KEY_STORE_ALIAS, true);

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void notAvailableIfKeyInitialisationError() throws UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, InvalidKeyException,
            KeyStoreManager.NewFingerprintEnrolledException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).initDefaultCipher();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onEncryptionFailed();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void fingerprintDisplayedIfCreationSuccessful() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.isCipherAvailable()).thenReturn(true);

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(mockSystem).showDialog();
    }
    
    @Test
    public void encryptionFailIfFingerprintNotPresent() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false);

        EncryptionCallbackAdapter callbackAdapter = Mockito.mock(EncryptionCallbackAdapter.class);
        String message = "message";
        jFingerprintManager.encrypt(message, callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onFingerprintNotAvailable();
        verify(callbackAdapter, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void encryptionFailIfMessageEmpty() {
        TextUtils mockTextUtils = Mockito.mock(TextUtils.class);
        when(mockTextUtils.isEmpty(anyString())).thenReturn(true);
        JFingerprintManager jFingerprintManager = createFingerPrintManagerWithTextUtils(mockTextUtils);
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.isCipherAvailable()).thenReturn(true);

        EncryptionCallbackAdapter callbackAdapter = Mockito.mock(EncryptionCallbackAdapter.class);
        jFingerprintManager.encrypt("", callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onEncryptionFailed();
        verify(callbackAdapter, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void encryptionDisplayedIfCreationSuccessful() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.isCipherAvailable()).thenReturn(true);

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(mockSystem).showDialog();
    }


    private class EncryptionCallbackAdapter implements JFingerprintManager.EncryptionCallback {

        @Override
        public void onEncryptionSuccess(String messageEncrypted) { }

        @Override
        public void onEncryptionFailed() { }

        @Override
        public void onFingerprintNotRecognized() { }

        @Override
        public void onAuthenticationFailedWithHelp(String help) { }

        @Override
        public void onFingerprintNotAvailable() { }
    }
}

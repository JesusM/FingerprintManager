package com.jesusm.jfingerprintmanager.encryption.presenter;

import com.jesusm.jfingerprintmanager.BaseTest;
import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    public void notAvailableIfCipherCreationError() throws NoSuchPaddingException, NoSuchAlgorithmException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new NoSuchAlgorithmException()).when(mockKeyStoreManager).createCipher();

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(encryptionCallback).onFingerprintNotAvailable();
        verify(encryptionCallback, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void fingerprintDisplayedIfCreationSuccessful() throws KeyStoreManager.InitialisationException, KeyStoreManager.NewFingerprintEnrolledException, NoSuchAlgorithmException, NoSuchPaddingException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.initDefaultCipher(anyString())).thenReturn(mock(Cipher.class));

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(mockSystem).showDialog();
    }
    
    @Test
    public void encryptionFailIfFingerprintNotPresent() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false);

        EncryptionCallbackAdapter callbackAdapter = Mockito.mock(EncryptionCallbackAdapter.class);
        jFingerprintManager.encrypt("message", callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onFingerprintNotAvailable();
        verify(callbackAdapter, never()).onEncryptionSuccess(anyString());
    }

    @Test
    public void encryptionFailIfMessageEmpty() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);

        EncryptionCallbackAdapter callbackAdapter = Mockito.mock(EncryptionCallbackAdapter.class);
        jFingerprintManager.encrypt("", callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onEncryptionFailed();
        verify(mockSystem, never()).showDialog();
    }

    @Test
    public void encryptionDisplayedIfCreationSuccessful() throws KeyStoreManager.InitialisationException, KeyStoreManager.NewFingerprintEnrolledException, NoSuchAlgorithmException, NoSuchPaddingException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.initDefaultCipher(anyString())).thenReturn(mock(Cipher.class));

        JFingerprintManager.EncryptionCallback encryptionCallback = Mockito.mock(JFingerprintManager.EncryptionCallback.class);
        jFingerprintManager.encrypt("message", encryptionCallback, mockFragmentManager);

        verify(mockSystem).showDialog();
    }

    @Test
    public void decryptionFailIfMessageEmpty() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);

        JFingerprintManager.DecryptionCallback callbackAdapter = Mockito.mock(JFingerprintManager.DecryptionCallback.class);
        jFingerprintManager.decrypt("", callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onDecryptionFailed();
        verify(mockSystem, never()).showDialog();
    }

    @Test
    public void decryptionFailIfMessageStructureNotCorrect() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);

        JFingerprintManager.DecryptionCallback callbackAdapter = Mockito.mock(JFingerprintManager.DecryptionCallback.class);
        jFingerprintManager.decrypt("message", callbackAdapter, mockFragmentManager);

        verify(callbackAdapter).onDecryptionFailed();
        verify(mockSystem, never()).showDialog();
    }

    @Test
    public void decryptionDisplayedIfCreationSuccessful() throws KeyStoreManager.InitialisationException, KeyStoreManager.NewFingerprintEnrolledException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.initCipherForDecryption(anyString(), any(byte[].class))).thenReturn(Mockito.mock(Cipher.class));

        JFingerprintManager.DecryptionCallback callbackAdapter = Mockito.mock(JFingerprintManager.DecryptionCallback.class);
        jFingerprintManager.decrypt("message:ivs", callbackAdapter, mockFragmentManager);

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

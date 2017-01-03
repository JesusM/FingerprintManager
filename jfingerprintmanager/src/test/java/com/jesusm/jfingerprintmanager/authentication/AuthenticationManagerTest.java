package com.jesusm.jfingerprintmanager.authentication;

import com.jesusm.jfingerprintmanager.BaseTest;
import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;

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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationManagerTest extends BaseTest{

    @Test
    public void notAvailableIfHardwareNotPresent() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(false);

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);

        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);
        verify(authenticationCallback).onFingerprintNotAvailable();
    }

    @Test
    public void notAvailableIfFingerprintNotEnrolled() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(false);

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfKeyStoreCreationError() throws KeyStoreException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new KeyStoreException()).when(mockKeyStoreManager).createKeyStore();

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfKeyGeneratorCreationError() throws NoSuchProviderException, NoSuchAlgorithmException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new NoSuchAlgorithmException()).when(mockKeyStoreManager).createKeyGenerator();

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfCipherCreationError() throws NoSuchPaddingException, NoSuchAlgorithmException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        doThrow(new NoSuchAlgorithmException()).when(mockKeyStoreManager).createCipher();

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfCipherInitialisationError() throws UnrecoverableKeyException,
            CertificateException, KeyStoreException, InvalidKeyException, IOException,
            NoSuchAlgorithmException, KeyStoreManager.NewFingerprintEnrolledException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).initDefaultCipher();

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfKeyCreationError() throws RuntimeException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).createKey(KEY_STORE_ALIAS, true);

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void notAvailableIfKeyInitialisationError() throws UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, InvalidKeyException,
            KeyStoreManager.NewFingerprintEnrolledException {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        doThrow(new RuntimeException()).when(mockKeyStoreManager).initDefaultCipher();

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(authenticationCallback).onFingerprintNotAvailable();
        verify(authenticationCallback, never()).onAuthenticationSuccess();
    }

    @Test
    public void authenticationDisplayedIfCreationSuccessful() {
        JFingerprintManager jFingerprintManager = createFingerPrintManager();
        when(mockFingerprintHardware.isFingerprintAuthAvailable()).thenReturn(true);
        when(mockKeyStoreManager.isFingerprintEnrolled()).thenReturn(true);
        when(mockKeyStoreManager.isCipherAvailable()).thenReturn(true);

        JFingerprintManager.AuthenticationCallback authenticationCallback = Mockito.mock(JFingerprintManager.AuthenticationCallback.class);
        jFingerprintManager.startAuthentication(authenticationCallback, mockFragmentManager);

        verify(mockSystem).showDialog();
    }
}

package com.jesusm.jfingerprintmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager;
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment;
import com.jesusm.jfingerprintmanager.base.ui.System;
import com.jesusm.jfingerprintmanager.encryption.Encoder;
import com.jesusm.jfingerprintmanager.utils.TextUtils;

import org.mockito.Mock;

import javax.crypto.Cipher;

public class BaseTest {
    protected static final String KEY_STORE_ALIAS = "fake_key";

    @Mock
    protected FingerprintHardware mockFingerprintHardware;
    @Mock
    protected KeyStoreManager mockKeyStoreManager;
    @Mock
    protected FragmentManager mockFragmentManager;
    @Mock
    protected FakeSystem mockSystem;
    @Mock
    private Context mockContext;

    @SuppressLint("VisibleForTests")
    protected JFingerprintManager createFingerPrintManager() {
        return createFingerPrintManagerWithTextUtils(new TextUtils());
    }

    @SuppressLint("VisibleForTests")
    protected JFingerprintManager createFingerPrintManagerWithTextUtils(TextUtils textUtils) {
        FingerprintAssetsManager fingerprintAssetsManager = new FingerprintAssetsManager(mockContext,
                mockFingerprintHardware, mockKeyStoreManager, KEY_STORE_ALIAS);
        return new JFingerprintManager(mockSystem, fingerprintAssetsManager, new FakeEncoder(), textUtils);
    }

    protected class FakeSystem implements System {

        @Override
        public void showDialog() { }

        @Override
        public void addDialogInfo(FingerprintBaseDialogFragment.Builder builder, FragmentManager fragmentManager) { }
    }

    public class FakeEncoder implements Encoder {

        @Override
        public String encrypt(String message, Cipher cipher) {
            return "";
        }
    }
}

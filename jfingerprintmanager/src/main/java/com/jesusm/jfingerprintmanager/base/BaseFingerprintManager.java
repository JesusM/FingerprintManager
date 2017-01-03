package com.jesusm.jfingerprintmanager.base;

import android.support.annotation.StyleRes;
import android.support.v4.app.FragmentManager;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment;
import com.jesusm.jfingerprintmanager.base.ui.System;

public class BaseFingerprintManager {
    protected FingerprintAssetsManager fingerprintAssetsManager;
    private System system;
    @StyleRes
    private int authenticationDialogStyle;

    public BaseFingerprintManager(FingerprintAssetsManager fingerprintAssetsManager, System system) {
        this.fingerprintAssetsManager = fingerprintAssetsManager;
        this.system = system;
    }

    protected void showFingerprintDialog(FingerprintBaseDialogFragment.Builder builder,
                                       FragmentManager fragmentManager,
                                       JFingerprintManager.FingerprintBaseCallback callback) {
        builder.withCallback(callback)
                .withCustomStyle(authenticationDialogStyle)
                .withFingerprintHardwareInformation(fingerprintAssetsManager);

        system.addDialogInfo(builder, fragmentManager);
        system.showDialog();
    }

    public void setAuthenticationDialogStyle(@StyleRes int style) {
        this.authenticationDialogStyle = style;
    }
}

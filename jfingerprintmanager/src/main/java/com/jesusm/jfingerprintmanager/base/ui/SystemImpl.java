package com.jesusm.jfingerprintmanager.base.ui;

import android.support.v4.app.FragmentManager;

public class SystemImpl implements System {
    private static final java.lang.String FINGERPRINT_DIALOG_TAG = "JFingerprintManager:fingerprintDialog";

    private FingerprintBaseDialogFragment fingerprintBaseDialogFragment;
    private FragmentManager fragmentManager;

    @Override
    public void showDialog() {
        fingerprintBaseDialogFragment.show(fragmentManager, FINGERPRINT_DIALOG_TAG);
    }

    @Override
    public void addDialogInfo(FingerprintBaseDialogFragment.Builder builder, FragmentManager fragmentManager) {
        this.fingerprintBaseDialogFragment = builder.build();
        this.fragmentManager = fragmentManager;
    }
}

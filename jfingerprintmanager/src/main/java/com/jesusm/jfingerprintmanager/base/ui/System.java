package com.jesusm.jfingerprintmanager.base.ui;

import android.support.v4.app.FragmentManager;

public interface System {
    void showDialog();

    void addDialogInfo(FingerprintBaseDialogFragment.Builder builder, FragmentManager fragmentManager);
}

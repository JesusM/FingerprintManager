package com.jesusm.jfingerprintmanager.utils;

import android.os.Build;

public class CompatUtils {
    public boolean isMarshmallow() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}

package com.jesusm.jfingerprintmanager.utils;

import android.support.annotation.Nullable;

public class TextUtils {
    public boolean isEmpty(@Nullable String text) {
        return text == null || text.length() == 0;
    }
}

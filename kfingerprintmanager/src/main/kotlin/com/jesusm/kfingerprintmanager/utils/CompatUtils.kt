package com.jesusm.kfingerprintmanager.utils

import android.os.Build
import android.os.Build.VERSION.SDK_INT

class CompatUtils {
    fun isMarshmallow() = SDK_INT >= Build.VERSION_CODES.M
    fun isN(): Boolean = SDK_INT >= Build.VERSION_CODES.N
}
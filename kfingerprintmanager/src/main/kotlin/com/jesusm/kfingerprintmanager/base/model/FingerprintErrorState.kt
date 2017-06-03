package com.jesusm.kfingerprintmanager.base.model

import android.support.annotation.StringRes
import com.jesusm.kfingerprintmanager.R

enum class FingerprintErrorState(@StringRes val errorMessage: Int) {
    FINGERPRINT_NOT_AVAILABLE(R.string.fingerprint_auth_not_available_msg),
    FINGERPRINT_INITIALISATION_ERROR(R.string.fingerprint_not_initialised_error_msg),
    LOCK_SCREEN_RESET_OR_DISABLED(-1),
    FINGERPRINT_NOT_ENROLLED(R.string.fingerprint_auth_not_available_msg),
    NEW_FINGERPRINT_ENROLLED(-1);
}
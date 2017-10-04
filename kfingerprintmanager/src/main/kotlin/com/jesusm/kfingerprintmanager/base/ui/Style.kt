package com.jesusm.kfingerprintmanager.base.ui

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import com.jesusm.kfingerprintmanager.R


data class Style(@StyleRes val style: Int = -1, @StringRes val titleText: Int = R.string.fingerprint_title,
                 @StringRes val descriptionText: Int = R.string.fingerprint_description,
                 @StringRes val checkText: Int = R.string.use_fingerprint_in_future,
                 @StringRes val passwordTextDescription: Int = R.string.fingerprint_description,
                 @DrawableRes val fingerprintIcon: Int = R.drawable.fingerprint_manager_icon_white_24dp)
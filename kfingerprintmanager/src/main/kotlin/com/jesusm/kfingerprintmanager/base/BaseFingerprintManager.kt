package com.jesusm.kfingerprintmanager.base

import android.support.v4.app.FragmentManager
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.ui.FingerprintBaseDialogFragment.Builder
import com.jesusm.kfingerprintmanager.base.ui.System

abstract class BaseFingerprintManager(val fingerprintAssetsManager: FingerprintAssetsManager,
                                      private val system: System) {

    var authenticationDialogStyle: Int = -1

    fun showFingerprintDialog(builder: Builder<*, *>,
                              fragmentManager: FragmentManager,
                              callback: KFingerprintManager.FingerprintBaseCallback) {
        builder.withCallback(callback)
                .withCustomStyle(authenticationDialogStyle)
                .withFingerprintHardwareInformation(fingerprintAssetsManager)

        system.addDialogInfo(builder, fragmentManager)
        system.showDialog()
    }
}
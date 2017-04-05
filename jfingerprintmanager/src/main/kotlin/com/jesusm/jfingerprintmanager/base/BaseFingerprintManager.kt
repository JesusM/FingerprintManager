package com.jesusm.jfingerprintmanager.base

import android.support.v4.app.FragmentManager
import com.jesusm.jfingerprintmanager.JFingerprintManager
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment.Builder
import com.jesusm.jfingerprintmanager.base.ui.System

abstract class BaseFingerprintManager(val fingerprintAssetsManager: FingerprintAssetsManager,
                                      private val system: System) {

    var authenticationDialogStyle: Int = -1

    fun showFingerprintDialog(builder: Builder<*, *>,
                              fragmentManager: FragmentManager,
                              callback: JFingerprintManager.FingerprintBaseCallback) {
        builder.withCallback(callback)
                .withCustomStyle(authenticationDialogStyle)
                .withFingerprintHardwareInformation(fingerprintAssetsManager)

        system.addDialogInfo(builder, fragmentManager)
        system.showDialog()
    }
}
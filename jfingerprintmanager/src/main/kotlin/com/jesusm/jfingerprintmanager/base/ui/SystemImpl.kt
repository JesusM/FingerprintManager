package com.jesusm.jfingerprintmanager.base.ui

import android.support.v4.app.FragmentManager

class SystemImpl : System {
    private val FINGERPRINT_DIALOG_TAG = "JFingerprintManager:fingerprintDialog"

    private var fingerprintBaseDialogFragment: FingerprintBaseDialogFragment<*>? = null
    private var dialogFragmentManager: FragmentManager? = null

    override fun showDialog() {
        fingerprintBaseDialogFragment?.show(dialogFragmentManager, FINGERPRINT_DIALOG_TAG)
    }

    override fun addDialogInfo(builder: FingerprintBaseDialogFragment.Builder<out FingerprintBaseDialogFragment<*>, *>?,
                               fragmentManager: FragmentManager?) {
        builder?.let {
            fingerprintBaseDialogFragment = it.build()
            dialogFragmentManager = fragmentManager
        }
    }
}
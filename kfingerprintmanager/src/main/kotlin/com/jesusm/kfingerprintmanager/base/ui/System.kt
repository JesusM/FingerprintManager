package com.jesusm.kfingerprintmanager.base.ui

interface System {
    fun showDialog()
    fun addDialogInfo(builder: FingerprintBaseDialogFragment.Builder<out FingerprintBaseDialogFragment<*>, *>?, fragmentManager: android.support.v4.app.FragmentManager?)
}
package com.jesusm.kfingerprintmanager.authentication

import android.support.v4.app.FragmentManager
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.authentication.ui.FingerprintAuthenticationDialogFragment
import com.jesusm.kfingerprintmanager.base.BaseFingerprintManager
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.model.FingerprintErrorState
import com.jesusm.kfingerprintmanager.base.ui.System

class AuthenticationManager(fingerprintAssetsManager: FingerprintAssetsManager,
                            system: System) : BaseFingerprintManager(fingerprintAssetsManager, system) {
    fun startAuthentication(authenticationCallback: KFingerprintManager.AuthenticationCallback,
                            fragmentManager: FragmentManager) {
        fingerprintAssetsManager.initSecureDependencies(object : KFingerprintManager.InitialisationCallback {
            override fun onErrorFingerprintNotInitialised() {
                authenticationCallback.onFingerprintNotAvailable()
            }

            override fun onErrorFingerprintNotEnrolled() {
                authenticationCallback.onFingerprintNotAvailable()
            }

            override fun onInitialisationSuccessfullyCompleted() {
                val errorState = fingerprintAssetsManager.errorState
                val builder = FingerprintAuthenticationDialogFragment.Builder()
                        .newFingerprintEnrolled(errorState == FingerprintErrorState.LOCK_SCREEN_RESET_OR_DISABLED)

                showFingerprintDialog(builder, fragmentManager, object : FingerprintAuthenticationDialogFragment.AuthenticationDialogCallback {
                    override fun onPasswordInserted(password: String) {
                        authenticationCallback.onSuccessWithManualPassword(password)
                    }

                    override fun createKey(invalidatedByBiometricEnrollment: Boolean) {
                        fingerprintAssetsManager.createKey(invalidatedByBiometricEnrollment)
                    }

                    override fun onFingerprintNotRecognized() {
                        authenticationCallback.onFingerprintNotRecognized()
                    }

                    override fun onAuthenticationFailedWithHelp(help: String?) {
                        authenticationCallback.onAuthenticationFailedWithHelp(help)
                    }

                    override fun onFingerprintNotAvailable() {
                        authenticationCallback.onFingerprintNotAvailable()
                    }

                    override fun onAuthenticationSuccess() {
                        authenticationCallback.onAuthenticationSuccess()
                    }

                    override fun onSuccessWithManualPassword(password: String) {
                        authenticationCallback.onSuccessWithManualPassword(password)
                    }

                    override fun onCancelled() {
                        authenticationCallback.onCancelled()
                    }
                })
            }

            override fun onFingerprintNotAvailable() {
                authenticationCallback.onFingerprintNotAvailable()
            }
        })
    }
}
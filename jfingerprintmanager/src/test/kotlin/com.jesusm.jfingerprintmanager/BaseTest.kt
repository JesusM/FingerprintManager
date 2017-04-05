package com.jesusm.jfingerprintmanager

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.app.FragmentManager
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.jfingerprintmanager.base.keystore.KeyStoreManager
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment
import com.jesusm.jfingerprintmanager.base.ui.System
import com.jesusm.jfingerprintmanager.encryption.Encoder
import com.nhaarman.mockito_kotlin.mock

open class BaseTest {
    companion object {
        val KEY_STORE_ALIAS: String = "fake_key"
    }

    val mockFingerprintHardware: FingerprintHardware = mock()
    val mockKeyStoreManager: KeyStoreManager = mock()
    val mockFragmentManager: FragmentManager = mock()
    val mockSystem: FakeSystem = mock()
    val mockContext: Context = mock()

    @SuppressLint("VisibleForTests")
    fun createFingerPrintManager(): JFingerprintManager {
        val fingerprintAssetsManager = FingerprintAssetsManager(mockContext,
                KEY_STORE_ALIAS, mockFingerprintHardware, mockKeyStoreManager)
        return JFingerprintManager(mockContext, KEY_STORE_ALIAS, mockSystem, fingerprintAssetsManager, FakeEncoder())
    }

    inner class FakeSystem : System {
        override fun showDialog() {

        }

        override fun addDialogInfo(builder: FingerprintBaseDialogFragment.Builder<out FingerprintBaseDialogFragment<*>, *>?, fragmentManager: FragmentManager?) {
        }
    }

    inner class FakeEncoder : Encoder {

        override fun encode(messageToEncode: ByteArray): String {
            return ""
        }

        override fun decode(messageToDecode: String): ByteArray {
            return ByteArray(0)
        }
    }
}
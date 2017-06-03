package com.jesusm.kfingerprintmanager

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.app.FragmentManager
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.kfingerprintmanager.base.keystore.KeyStoreManager
import com.jesusm.kfingerprintmanager.base.ui.FingerprintBaseDialogFragment
import com.jesusm.kfingerprintmanager.base.ui.System
import com.jesusm.kfingerprintmanager.encryption.Encoder
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
    fun createFingerPrintManager(): KFingerprintManager {
        val fingerprintAssetsManager = FingerprintAssetsManager(mockContext,
                KEY_STORE_ALIAS, mockFingerprintHardware, mockKeyStoreManager)
        return KFingerprintManager(mockContext, KEY_STORE_ALIAS, mockSystem, fingerprintAssetsManager, FakeEncoder())
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
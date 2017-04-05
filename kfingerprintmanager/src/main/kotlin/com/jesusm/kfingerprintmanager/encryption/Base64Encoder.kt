package com.jesusm.kfingerprintmanager.encryption

import android.util.Base64

class Base64Encoder : Encoder {
    override fun encode(messageToEncode: ByteArray): String = Base64.encodeToString(messageToEncode, Base64.DEFAULT)

    override fun decode(messageToDecode: String): ByteArray = Base64.decode(messageToDecode, Base64.DEFAULT)
}
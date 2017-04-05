package com.jesusm.jfingerprintmanager.encryption

interface Encoder {
    fun encode(messageToEncode: ByteArray) : String
    fun decode(messageToDecode : String) : ByteArray
}
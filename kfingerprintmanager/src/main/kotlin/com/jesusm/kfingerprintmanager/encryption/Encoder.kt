package com.jesusm.kfingerprintmanager.encryption

interface Encoder {
    fun encode(messageToEncode: ByteArray): String
    fun decode(messageToDecode: String): ByteArray
}
package com.jesusm.kfingerprintmanager.encryption

class EncryptionData(var encryptedMessage: String, var encryptedIVs: String?,
                     val encoder: Encoder, val separator: String = ":") {

    constructor(encryptedMessageString: String, encoder: Encoder) :
            this(encryptedMessageString, encryptedMessageString, encoder, ":") {
        val split = encryptedMessageString.split(separator)
        encryptedMessage = split[0]
        if (split.size > 1) {
            encryptedIVs = split[1]
        } else {
            encryptedIVs = null
        }
    }

    constructor(encryptedMessage: ByteArray, encryptedIVs: ByteArray, encoder: Encoder) :
            this(encoder.encode(encryptedMessage), encoder.encode(encryptedIVs), encoder)

    fun print(): String = encryptedMessage + separator + encryptedIVs
    fun dataIsCorrect(): Boolean = separator.isNullOrEmpty().not() && encryptedIVs.isNullOrEmpty().not()

    fun decodedMessage(): ByteArray = encoder.decode(encryptedMessage)
    fun decodedIVs(): ByteArray? = if (encryptedIVs != null) encoder.decode(encryptedIVs as String) else null
}
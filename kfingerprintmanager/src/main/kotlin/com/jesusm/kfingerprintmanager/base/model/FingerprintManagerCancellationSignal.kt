package com.jesusm.kfingerprintmanager.base.model

import android.support.v4.os.CancellationSignal

class FingerprintManagerCancellationSignal(var cancellationSignal: CancellationSignal? = null) {
    fun start() {
        cancel()
        cancellationSignal = CancellationSignal()
    }

    fun cancel() {
        if (isCancelled().not()) {
            cancellationSignal?.cancel()
            cancellationSignal = null
        }
    }

    fun isCancelled(): Boolean = cancellationSignal == null
}
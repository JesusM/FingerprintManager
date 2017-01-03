package com.jesusm.jfingerprintmanager.base.model;

import android.support.v4.os.CancellationSignal;

public class FingerprintManagerCancellationSignal {
    private CancellationSignal cancellationSignal;

    public CancellationSignal getCancellationSignal() {
        return cancellationSignal;
    }

    public void start() {
        cancel();
        cancellationSignal = new CancellationSignal();
    }

    public boolean isCancelled() {
        return cancellationSignal == null;
    }

    public void cancel() {
        if (!isCancelled()) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }
}

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.jesusm.jfingerprintmanager.base.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.R;
import com.jesusm.jfingerprintmanager.base.FingerprintAssetsManager;
import com.jesusm.jfingerprintmanager.base.FingerprintBaseDialogPresenter;
import com.jesusm.jfingerprintmanager.base.hardware.FingerprintHardware;

public class FingerprintBaseDialogFragment<P extends FingerprintBaseDialogPresenter> extends AppCompatDialogFragment
        implements FingerprintBaseDialogPresenter.View {

    protected JFingerprintManager.FingerprintBaseCallback callback;

    protected View fingerprintContainer;
    protected AlertDialog alertDialog;
    private int customDialogStyle = 0;

    private P presenter;
    protected View dialogRootView;

    public FingerprintBaseDialogFragment() {
    }

    public void setCallback(@NonNull JFingerprintManager.FingerprintBaseCallback callback) {
        this.callback = callback;
    }

    protected void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = buildDialogContext();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogRootView = layoutInflater.inflate(R.layout.fingerprint_dialog_container, null, false);

        inflateViews(dialogRootView);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, customDialogStyle);
        builder.setView(dialogRootView);

        addDialogButtons(builder);

        alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                onDialogShown();
            }
        });

        return alertDialog;
    }

    @CallSuper
    protected void onDialogShown()
    {
        presenter.onViewShown();
    }

    @CallSuper
    protected void inflateViews(View rootView) {
        fingerprintContainer = rootView.findViewById(R.id.fingerprint_dialog_content);
    }

    @CallSuper
    protected void addDialogButtons(AlertDialog.Builder alertDialogBuilder) {
        alertDialogBuilder.setNegativeButton(R.string.cancel, null);
    }

    private Context buildDialogContext() {
        if (customDialogStyle == -1) {
            return getContext();
        }

        return new ContextThemeWrapper(getContext(), customDialogStyle);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    protected void setCustomDialogStyle(@StyleRes int customDialogStyle) {
        this.customDialogStyle = customDialogStyle;
    }

    @Override
    public void onFingerprintNotAvailable() {
        callback.onFingerprintNotAvailable();
    }

    @Override
    public void onFingerprintNotRecognized() {
        callback.onFingerprintNotRecognized();
    }

    @Override
    public void onAuthenticationFailedWithHelp(String help) {
        callback.onAuthenticationFailedWithHelp(help);
    }

    @Override
    public void onFingerprintDisplayed() {
        updateDialogButtonText(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
        fingerprintContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void close() {
        dismiss();
    }

    protected void updateDialogButtonText(int whichButton, @StringRes int resId) {
        alertDialog.getButton(whichButton).setText(resId);
    }

    public abstract static class Builder<D extends FingerprintBaseDialogFragment<P>,
            P extends FingerprintBaseDialogPresenter> {
        private int customStyle;
        private JFingerprintManager.FingerprintBaseCallback callback;
        private FingerprintHardware fingerPrintHardware;
        private FingerprintManagerCompat.CryptoObject cryptoObject;

        public Builder withCustomStyle(int customStyle) {
            this.customStyle = customStyle;
            return this;
        }

        public Builder withCallback(@NonNull JFingerprintManager.FingerprintBaseCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder withFingerprintHardwareInformation(FingerprintAssetsManager fingerprintAssetsManager) {
            this.fingerPrintHardware = fingerprintAssetsManager.getFingerprintHardware();
            this.cryptoObject = fingerprintAssetsManager.getCryptoObject();

            return this;
        }

        D build() throws RuntimeException {
            if (callback == null) {
                throw new RuntimeException("You need to provide a callback");
            }

            D dialogFragment = createDialogFragment();
            dialogFragment.setCallback(callback);

            P presenter = createPresenter(dialogFragment);
            dialogFragment.setPresenter(presenter);
            if (fingerPrintHardware != null) {
                presenter.setFingerprintHardware(fingerPrintHardware, cryptoObject);
            }

            if (customStyle != -1) {
                dialogFragment.setCustomDialogStyle(customStyle);
            }

            addProperties(dialogFragment);

            return dialogFragment;
        }

        protected abstract D createDialogFragment();

        protected abstract P createPresenter(D view);

        protected void addProperties(D dialogFragment) { }
    }
}

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

package com.jesusm.jfingerprintmanager.authentication.ui;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jesusm.jfingerprintmanager.R;
import com.jesusm.jfingerprintmanager.JFingerprintManager;
import com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter;
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment;

public class FingerprintAuthenticationDialogFragment extends FingerprintBaseDialogFragment<FingerprintAuthenticationDialogPresenter>
        implements TextView.OnEditorActionListener, FingerprintAuthenticationDialogPresenter.View {

    public interface AuthenticationDialogCallback extends JFingerprintManager.AuthenticationCallback {
        void createKey(boolean invalidatedByBiometricEnrollment);

        void onPasswordInserted(String password);
    }

    private AuthenticationDialogCallback callback;
    private View passwordContainer;
    private EditText password;
    private TextInputLayout textInputLayout;
    private CheckBox useFingerprintFutureCheckBox;

    private SharedPreferences sharedPreferences;
    private boolean startWithNewFingerprintEnrolled;

    private FingerprintAuthenticationDialogPresenter presenter;

    public FingerprintAuthenticationDialogFragment() {
    }

    @Override
    protected void onDialogShown() {
        super.onDialogShown();

        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showPasswordClicked();
            }
        });
    }

    @Override
    protected void addDialogButtons(AlertDialog.Builder alertDialogBuilder) {
        super.addDialogButtons(alertDialogBuilder);

        alertDialogBuilder.setPositiveButton(R.string.use_password, null);
    }

    @Override
    protected void setPresenter(FingerprintAuthenticationDialogPresenter presenter) {
        super.setPresenter(presenter);

        this.presenter = presenter;
        if (startWithNewFingerprintEnrolled) {
            this.presenter.newFingerprintEnrolled();
        }
    }

    @Override
    public void setCallback(@NonNull JFingerprintManager.FingerprintBaseCallback callback) {
        super.setCallback(callback);
        this.callback = (AuthenticationDialogCallback) callback;
    }

    @Override
    protected void inflateViews(View rootView) {
        super.inflateViews(rootView);

        passwordContainer = rootView.findViewById(R.id.fingerprint_dialog_backup_content);
        textInputLayout = (TextInputLayout) rootView.findViewById(R.id.input_layout_password);
        password = (EditText) rootView.findViewById(R.id.password);
        password.setOnEditorActionListener(this);
        password.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    onPasswordEmpty();
                } else {
                    textInputLayout.setError(null);
                }

            }
        });
        useFingerprintFutureCheckBox = (CheckBox) rootView.findViewById(R.id.use_fingerprint_in_future_check);
    }

    private void startWithNewFingerprintEnrolled() {
        this.startWithNewFingerprintEnrolled = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void saveUseFingerprintFuture(boolean useFingerprintFuture) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.use_fingerprint_to_authenticate_key), useFingerprintFuture);
        editor.apply();
    }

    @Override
    public void createKey() {
        callback.createKey(true);
    }

    @Override
    public void onFingerprintDisplayed() {
        fingerprintContainer.setVisibility(View.VISIBLE);
        passwordContainer.setVisibility(View.GONE);
    }

    @Override
    public void onPasswordViewDisplayed(boolean fingerprintEnrolled) {
        updateDialogButtonText(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
        updateDialogButtonText(DialogInterface.BUTTON_POSITIVE, R.string.ok);

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordText = password.getText().toString();
                boolean useFingerprintFuture = useFingerprintFutureCheckBox.isChecked();
                presenter.onPasswordEntered(passwordText, useFingerprintFuture);
            }
        });

        fingerprintContainer.setVisibility(View.GONE);
        showWithRevealEffect(passwordContainer);
        password.requestFocus();

        if (fingerprintEnrolled) {
            password.setHint(R.string.new_fingerprint_enrolled_description);
            useFingerprintFutureCheckBox.setVisibility(View.VISIBLE);
        }
    }

    private void showWithRevealEffect(View viewToShow) {
        int centerX = dialogRootView.getMeasuredWidth() / 2;
        int centerY = dialogRootView.getMeasuredHeight() / 2;
        int endRadius = dialogRootView.getMeasuredWidth() / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(viewToShow, centerX,
                centerY, 0, endRadius);

        viewToShow.setVisibility(View.VISIBLE);
        anim.start();
    }

    @Override
    public void onPasswordInserted(String password) {
        callback.onPasswordInserted(password);
    }

    @Override
    public void onPasswordEmpty() {
        textInputLayout.setError("Password can't be empty");
    }

    @Override
    public void onAuthenticationSucceed() {
        callback.onAuthenticationSuccess();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            dismiss();
            return true;
        } else {
            textInputLayout.setError(null);
        }

        return false;
    }

    public static class Builder extends FingerprintBaseDialogFragment.Builder
            <FingerprintAuthenticationDialogFragment, FingerprintAuthenticationDialogPresenter> {
        boolean newFingerprintEnrolled;

        public Builder newFingerprintEnrolled(boolean newFingerprintEnrolled) {
            this.newFingerprintEnrolled = newFingerprintEnrolled;
            return this;
        }

        @Override
        protected FingerprintAuthenticationDialogFragment createDialogFragment() {
            return new FingerprintAuthenticationDialogFragment();
        }

        @Override
        protected void addProperties(FingerprintAuthenticationDialogFragment dialogFragment) {
            if (newFingerprintEnrolled) {
                dialogFragment.startWithNewFingerprintEnrolled();
            }
        }

        @Override
        protected FingerprintAuthenticationDialogPresenter createPresenter(FingerprintAuthenticationDialogFragment dialogFragment) {
            return new FingerprintAuthenticationDialogPresenter(dialogFragment);
        }
    }

    private static class TextWatcherAdapter implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}

package com.jesusm.jfingerprintmanager.authentication.ui

import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.jesusm.jfingerprintmanager.JFingerprintManager
import com.jesusm.jfingerprintmanager.R
import com.jesusm.jfingerprintmanager.authentication.presenter.FingerprintAuthenticationDialogPresenter
import com.jesusm.jfingerprintmanager.base.ui.FingerprintBaseDialogFragment

class FingerprintAuthenticationDialogFragment : FingerprintBaseDialogFragment<FingerprintAuthenticationDialogPresenter>(),
        TextView.OnEditorActionListener, FingerprintAuthenticationDialogPresenter.View {

    interface AuthenticationDialogCallback : JFingerprintManager.AuthenticationCallback {
        fun createKey(invalidatedByBiometricEnrollment: Boolean)

        fun onPasswordInserted(password: String)
    }

    val passwordContainer : View by lazy {
        dialogRootView.findViewById(R.id.fingerprint_dialog_backup_content)
    }
    val password by lazy {
        dialogRootView.findViewById(R.id.password) as EditText
    }

    val textInputLayout by lazy {
        dialogRootView.findViewById(R.id.input_layout_password) as TextInputLayout
    }

    val useFingerprintFutureCheckBox by lazy {
        dialogRootView.findViewById(R.id.use_fingerprint_in_future_check) as CheckBox
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    private var startWithNewFingerprintEnrolled: Boolean = false

    fun onPresenterChanged(new: FingerprintAuthenticationDialogPresenter) {
        if (startWithNewFingerprintEnrolled) {
            new.newFingerprintEnrolled()
        }
    }

    override fun inflateViews(rootView: View) {
        super.inflateViews(rootView)

        rootView.apply {
            password.setOnEditorActionListener(this@FingerprintAuthenticationDialogFragment)
            password.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    if (s.toString().isEmpty()) {
                        onPasswordEmpty()
                    } else {
                        textInputLayout.error = null
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        presenter?.pause()
    }

    override fun onDialogShown() {
        super.onDialogShown()

        alertDialog.apply {
            val b = getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener {
                presenter?.showPasswordClicked()
            }
        }
    }

    override fun addDialogButtons(dialogBuilder: AlertDialog.Builder) {
        super.addDialogButtons(dialogBuilder)

        dialogBuilder.setPositiveButton(R.string.use_password, null)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return when (actionId) {
            EditorInfo.IME_ACTION_GO -> {
                dismiss()
                true
            }
            else -> {
                textInputLayout.error = null
                false
            }
        }
    }

    override fun onFingerprintDisplayed() {
        fingerprintContainer?.visibility = VISIBLE
        passwordContainer.visibility = GONE
    }

    override fun onPasswordViewDisplayed(newFingerprintEnrolled: Boolean) {
        updateDialogButtonText(DialogInterface.BUTTON_NEGATIVE, R.string.cancel)
        updateDialogButtonText(DialogInterface.BUTTON_POSITIVE, R.string.ok)

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            val passwordText = password.text?.toString()
            val useFingerprintFuture = useFingerprintFutureCheckBox.isChecked
            if (passwordText != null)
                presenter?.onPasswordEntered(passwordText, useFingerprintFuture)
        }

        fingerprintContainer?.let {
            it.visibility = GONE
            showWithRevealEffect(it)
        }
        password.requestFocus()

        if (newFingerprintEnrolled) {
            password.hint = getString(R.string.new_fingerprint_enrolled_description)
            useFingerprintFutureCheckBox.visibility = VISIBLE
        }
    }

    private fun showWithRevealEffect(viewToShow: View) {
        dialogRootView.let {
            val centerX = it.measuredWidth / 2
            val centerY = it.measuredHeight / 2
            val endRadius = it.measuredWidth / 2
            val anim = ViewAnimationUtils.createCircularReveal(viewToShow, centerX,
                    centerY, 0f, endRadius.toFloat())

            viewToShow.visibility = VISIBLE
            anim.start()
        }
    }

    fun startWithNewFingerprintEnrolled() {
        startWithNewFingerprintEnrolled = true
    }


    override fun saveUseFingerprintFuture(useFingerprintFuture: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(getString(R.string.use_fingerprint_to_authenticate_key), useFingerprintFuture)
        editor.apply()
    }

    override fun createKey() {
        (callback as AuthenticationDialogCallback).createKey(true)
    }

    override fun onPasswordInserted(password: String) {
        (callback as AuthenticationDialogCallback).onPasswordInserted(password)
    }

    override fun onPasswordEmpty() {
        textInputLayout.error = getString(R.string.warning_password_empty)
    }

    override fun onAuthenticationSucceed() {
        (callback as AuthenticationDialogCallback).onAuthenticationSuccess()
    }

    class Builder : FingerprintBaseDialogFragment.Builder<FingerprintAuthenticationDialogFragment, FingerprintAuthenticationDialogPresenter>() {
        internal var newFingerprintEnrolled: Boolean = false

        fun newFingerprintEnrolled(newFingerprintEnrolled: Boolean): Builder {
            this.newFingerprintEnrolled = newFingerprintEnrolled
            return this
        }

        override fun createDialogFragment(): FingerprintAuthenticationDialogFragment {
            return FingerprintAuthenticationDialogFragment()
        }

        override fun addProperties(dialogFragment: FingerprintAuthenticationDialogFragment) {
            if (newFingerprintEnrolled) {
                dialogFragment.startWithNewFingerprintEnrolled()
            }
        }

        override fun createPresenter(view: FingerprintAuthenticationDialogFragment): FingerprintAuthenticationDialogPresenter {
            return FingerprintAuthenticationDialogPresenter(view)
        }
    }

    private open class TextWatcherAdapter : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {}
    }
}
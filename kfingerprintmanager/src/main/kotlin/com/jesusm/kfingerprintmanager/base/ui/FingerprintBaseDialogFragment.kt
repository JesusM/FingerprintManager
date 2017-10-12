package com.jesusm.kfingerprintmanager.base.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.StringRes
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.R
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.kfingerprintmanager.base.ui.presenter.FingerprintBaseDialogPresenter
import kotlin.properties.Delegates

abstract class FingerprintBaseDialogFragment<T : FingerprintBaseDialogPresenter> : AppCompatDialogFragment(),
        FingerprintBaseDialogPresenter.View {

    var callback: KFingerprintManager.FingerprintBaseCallback? = null
    lateinit var dialogRootView: View
    lateinit var fingerprintContainer: View
    lateinit var alertDialog: AlertDialog
    private var customDialogStyle: Int = 0

    var presenter by Delegates.observable<T?>(null) {
        _, _, new ->
        onPresenterChanged(new)
    }

    open fun onPresenterChanged(new: T?) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = buildDialogContext()
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        dialogRootView = layoutInflater.inflate(R.layout.fingerprint_dialog_container, null, false)
        fingerprintContainer = dialogRootView.findViewById(R.id.fingerprint_dialog_content)
        inflateViews(dialogRootView)
        applyStyleToView(dialogRootView)

        val builder = AlertDialog.Builder(context, customDialogStyle)
        builder.setView(dialogRootView)

        addDialogButtons(builder)

        return builder.create().apply {
            alertDialog = this
            setOnShowListener({ onDialogShown() })
        }
    }

    @CallSuper
    open fun inflateViews(rootView: View) {}

    private fun applyStyleToView(rootView: View) {
        if (style.style != -1) {
            customDialogStyle = style.style
        }

        rootView.apply {
            val titleView = findViewById<TextView>(R.id.fingerprint_dialog_title)
            titleView.text = getString(style.titleText)
            val descriptionView = findViewById<TextView>(R.id.fingerprint_dialog_description)
            descriptionView.text = getString(style.descriptionText)
            val checkboxView = findViewById<TextView>(R.id.use_fingerprint_in_future_check)
            checkboxView.text = getString(style.checkText)
            val passwordView = findViewById<EditText>(R.id.password)
            passwordView.hint = getString(style.passwordTextDescription)
            findViewById<ImageView>(R.id.fingerprint_icon).setImageResource(style.fingerprintIcon)
        }
    }

    @CallSuper
    open fun onDialogShown() {
        presenter?.onViewShown()
    }

    @CallSuper
    open fun addDialogButtons(dialogBuilder: AlertDialog.Builder) {
        dialogBuilder.setNegativeButton(R.string.cancel, { _, _ -> presenter?.onDialogCancelled() })
    }

    override fun onPause() {
        super.onPause()
        presenter?.pause()
    }

    private fun buildDialogContext(): Context =
            if (customDialogStyle == 1) context else ContextThemeWrapper(context, customDialogStyle)

    override fun onFingerprintDisplayed() {
        updateDialogButtonText(DialogInterface.BUTTON_NEGATIVE, R.string.cancel)
        fingerprintContainer.visibility = View.VISIBLE
    }

    fun updateDialogButtonText(whichButton: Int, @StringRes resId: Int) {
        alertDialog.getButton(whichButton)?.setText(resId)
    }

    override fun onCancelled() {
        callback?.onCancelled()
    }

    override fun close() {
        dismiss()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        presenter?.onDialogCancelled()
    }

    override fun onFingerprintNotRecognized() {
        callback?.onFingerprintNotRecognized()
    }

    override fun onAuthenticationFailedWithHelp(help: String?) {
        callback?.onAuthenticationFailedWithHelp(help)
    }

    override fun onFingerprintNotAvailable() {
        callback?.onFingerprintNotAvailable()
    }

    private lateinit var style: Style

    fun applyStyle(style: Style) {
        this.style = style
    }

    abstract class Builder<D : FingerprintBaseDialogFragment<P>, P : FingerprintBaseDialogPresenter> {
        private var customStyle: Style = Style()
        private var callback: KFingerprintManager.FingerprintBaseCallback? = null
        private lateinit var fingerPrintHardware: FingerprintHardware
        private lateinit var cryptoObject: FingerprintManagerCompat.CryptoObject

        fun withCustomStyle(customStyle: Style): Builder<*, *> {
            this.customStyle = customStyle
            return this
        }

        fun withCallback(callback: KFingerprintManager.FingerprintBaseCallback): Builder<*, *> {
            this.callback = callback
            return this
        }

        fun withFingerprintHardwareInformation(fingerprintAssetsManager: FingerprintAssetsManager): Builder<*, *> {
            this.fingerPrintHardware = fingerprintAssetsManager.fingerprintHardware
            this.cryptoObject = fingerprintAssetsManager.getCryptoObject()

            return this
        }

        @Throws(RuntimeException::class)
        internal fun build(): D {
            if (callback == null) {
                throw RuntimeException("You need to provide a callback")
            }

            val dialogFragment = createDialogFragment()
            dialogFragment.callback = callback

            val presenter = createPresenter(dialogFragment)
            dialogFragment.presenter = presenter
            presenter.setFingerprintHardware(fingerPrintHardware, cryptoObject)
            dialogFragment.applyStyle(customStyle)

            addProperties(dialogFragment)

            return dialogFragment
        }

        protected abstract fun createDialogFragment(): D

        protected abstract fun createPresenter(view: D): P

        protected abstract fun addProperties(dialogFragment: D)
    }
}
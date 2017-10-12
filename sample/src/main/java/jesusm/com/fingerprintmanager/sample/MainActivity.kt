package jesusm.com.fingerprintmanager.sample

import android.os.Bundle
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.utils.bind

class MainActivity : AppCompatActivity() {
    private val KEY = "my_key"

    private var messageTextView = bind<TextView>(R.id.message)
    private var messageToBeEncryptedEditText = bind<EditText>(R.id.editText)
    private var authenticateButton = bind<Button>(R.id.buttonAuthenticate)
    private var encryptTextButton = bind<Button>(R.id.buttonEncrypt)
    private var decryptTextButton = bind<Button>(R.id.buttonDecrypt)
    @StyleRes
    private var dialogTheme: Int = 0

    private var messageToDecrypt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectView(findViewById(R.id.buttonDialogThemeLight))

        initClickListeners()
    }

    private fun initClickListeners() {

        findViewById<Button>(R.id.buttonDialogThemeLight).setOnClickListener { v ->
            selectView(v)

            deselectView(findViewById(R.id.buttonDialogThemeDark))

            dialogTheme = R.style.DialogThemeLight
        }

        findViewById<Button>(R.id.buttonDialogThemeDark).setOnClickListener { v ->
            selectView(v)

            deselectView(findViewById(R.id.buttonDialogThemeLight))

            dialogTheme = R.style.DialogThemeDark
        }

        authenticateButton.value.setOnClickListener {
            createFingerprintManagerInstance().authenticate(object : KFingerprintManager.AuthenticationCallback {
                override fun onAuthenticationFailedWithHelp(help: String?) {
                    messageTextView.value.text = help
                }

                override fun onAuthenticationSuccess() {
                    messageTextView.value.text = "Successfully authenticated"
                }

                override fun onSuccessWithManualPassword(password: String) {
                    messageTextView.value.text = "Manual password: " + password
                }

                override fun onFingerprintNotRecognized() {
                    messageTextView.value.text = "Fingerprint not recognized"
                }

                override fun onFingerprintNotAvailable() {
                    messageTextView.value.text = "Fingerprint not available"
                }

                override fun onCancelled() {
                    messageTextView.value.text = "Operation cancelled by user"
                }
            }, supportFragmentManager)
        }

        encryptTextButton.value.setOnClickListener {
            messageToDecrypt = messageToBeEncryptedEditText.value.text.toString()
            createFingerprintManagerInstance().encrypt(messageToDecrypt, object : KFingerprintManager.EncryptionCallback {
                override fun onFingerprintNotRecognized() {
                    messageTextView.value.text = "Fingerprint not recognized"
                }

                override fun onAuthenticationFailedWithHelp(help: String?) {
                    messageTextView.value.text = help
                }

                override fun onFingerprintNotAvailable() {
                    messageTextView.value.text = "Fingerprint not available"
                }

                override fun onEncryptionSuccess(messageEncrypted: String) {
                    val message = getString(R.string.encrypt_message_success, messageEncrypted)
                    messageTextView.value.text = message
                    messageToBeEncryptedEditText.value.setText(messageEncrypted)
                    encryptTextButton.value.visibility = View.GONE
                    decryptTextButton.value.visibility = View.VISIBLE
                }

                override fun onEncryptionFailed() {
                    messageTextView.value.text = "Encryption failed"
                }

                override fun onCancelled() {
                    messageTextView.value.text = "Operation cancelled by user"
                }
            }, supportFragmentManager)
        }

        decryptTextButton.value.setOnClickListener {
            messageToDecrypt = messageToBeEncryptedEditText.value.text.toString()
            createFingerprintManagerInstance().decrypt(messageToDecrypt, object : KFingerprintManager.DecryptionCallback {
                override fun onDecryptionSuccess(messageDecrypted: String) {
                    val message = getString(R.string.decrypt_message_success, messageDecrypted)
                    messageTextView.value.text = message
                    messageToBeEncryptedEditText.value.setText("")
                    decryptTextButton.value.visibility = View.GONE
                    encryptTextButton.value.visibility = View.VISIBLE
                }

                override fun onDecryptionFailed() {
                    messageTextView.value.text = "Decryption failed"
                }

                override fun onFingerprintNotRecognized() {
                    messageTextView.value.text = "Fingerprint not recognized"
                }

                override fun onAuthenticationFailedWithHelp(help: String?) {
                    messageTextView.value.text = help
                }

                override fun onFingerprintNotAvailable() {
                    messageTextView.value.text = "Fingerprint not available"
                }

                override fun onCancelled() {
                    messageTextView.value.text = "Operation cancelled by user"
                }
            }, supportFragmentManager)
        }
    }

    private fun createFingerprintManagerInstance(): KFingerprintManager {
        val fingerprintManager = KFingerprintManager(this, KEY)
        fingerprintManager.setAuthenticationDialogStyle(dialogTheme)
        return fingerprintManager
    }

    private fun selectView(view: View) {
        view.apply {
            isSelected = true
            elevation = 32f
        }
    }

    private fun deselectView(view: View) {
        view.apply {
            isSelected = false
            elevation = 0f
        }
    }
}

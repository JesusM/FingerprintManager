package jesusm.com.fingerprintmanager.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jesusm.jfingerprintmanager.JFingerprintManager;

public class MainActivity extends AppCompatActivity {

    private static final String KEY = "my_key";
    private TextView messageTextView;
    private EditText messageToBeEncryptedEditText;
    private Button authenticateButton;
    private Button encryptTextButton;
    private Button decryptTextButton;

    private String messageToDecrypt;
    @StyleRes
    private int dialogTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initClickListeners();
    }

    private void initViews() {
        messageTextView = (TextView) findViewById(R.id.message);
        messageToBeEncryptedEditText = (EditText) findViewById(R.id.editText);
        authenticateButton = (Button) findViewById(R.id.buttonAuthenticate);
        encryptTextButton = (Button) findViewById(R.id.buttonEncrypt);
        decryptTextButton = (Button) findViewById(R.id.buttonDecrypt);

        selectView(findViewById(R.id.buttonDialogThemeLight));
    }

    private JFingerprintManager createFingerprintManagerInstance() {
        JFingerprintManager fingerprintManager = new JFingerprintManager(this, KEY);
        fingerprintManager.setAuthenticationDialogStyle(dialogTheme);
        return fingerprintManager;
    }

    private void initClickListeners() {

        findViewById(R.id.buttonDialogThemeLight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectView(v);

                deselectView(findViewById(R.id.buttonDialogThemeDark));

                dialogTheme = R.style.DialogThemeLight;
            }
        });

        findViewById(R.id.buttonDialogThemeDark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectView(v);

                deselectView(findViewById(R.id.buttonDialogThemeLight));

                dialogTheme = R.style.DialogThemeDark;
            }
        });

        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFingerprintManagerInstance().startAuthentication(new JFingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        messageTextView.setText("Successfully authenticated");
                    }

                    @Override
                    public void onSuccessWithManualPassword(@NonNull String password) {
                        messageTextView.setText("Manual password: " + password);
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        messageTextView.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(String help) {
                        messageTextView.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageTextView.setText("Fingerprint not available");
                    }

                    @Override
                    public void onCancelled() {
                        messageTextView.setText("Operation cancelled by user");
                    }
                }, getSupportFragmentManager());
            }
        });

        encryptTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToDecrypt = messageToBeEncryptedEditText.getText().toString();
                createFingerprintManagerInstance().encrypt(messageToDecrypt, new JFingerprintManager.EncryptionCallback() {
                    @Override
                    public void onFingerprintNotRecognized() {
                        messageTextView.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(String help) {
                        messageTextView.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageTextView.setText("Fingerprint not available");
                    }

                    @Override
                    public void onEncryptionSuccess(String messageEncrypted) {
                        String message = getString(R.string.encrypt_message_success, messageEncrypted);
                        messageTextView.setText(message);
                        messageToBeEncryptedEditText.setText(messageEncrypted);
                        encryptTextButton.setVisibility(View.GONE);
                        decryptTextButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onEncryptionFailed() {
                        messageTextView.setText("Encryption failed");
                    }

                    @Override
                    public void onCancelled() {
                        messageTextView.setText("Operation cancelled by user");
                    }

                }, getSupportFragmentManager());
            }
        });

        decryptTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToDecrypt = messageToBeEncryptedEditText.getText().toString();
                createFingerprintManagerInstance().decrypt(messageToDecrypt, new JFingerprintManager.DecryptionCallback() {
                    @Override
                    public void onDecryptionSuccess(String messageDecrypted) {
                        String message = getString(R.string.decrypt_message_success, messageDecrypted);
                        messageTextView.setText(message);
                        messageToBeEncryptedEditText.setText("");
                        decryptTextButton.setVisibility(View.GONE);
                        encryptTextButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onDecryptionFailed() {
                        messageTextView.setText("Decryption failed");
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        messageTextView.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(String help) {
                        messageTextView.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageTextView.setText("Fingerprint not available");
                    }

                    @Override
                    public void onCancelled() {
                        messageTextView.setText("Operation cancelled by user");
                    }
                }, getSupportFragmentManager());
            }
        });
    }

    private void selectView(View view) {
        view.setSelected(true);
        view.setElevation(32);
    }

    private void deselectView(View view) {
        view.setSelected(false);
        view.setElevation(0);
    }
}

package jesusm.com.fingerprintmanager.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private JFingerprintManager fingerPrintManager;

    private String messageToBeEncrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initFingerprintManager();

        initClickListeners();
    }

    private void initViews() {
        messageTextView = (TextView) findViewById(R.id.message);
        messageToBeEncryptedEditText = (EditText) findViewById(R.id.editText);
        authenticateButton = (Button) findViewById(R.id.buttonAuthenticate);
        encryptTextButton = (Button) findViewById(R.id.buttonEncrypt);

        selectView(findViewById(R.id.buttonDialogThemeLight));
    }

    private void initFingerprintManager() {
        fingerPrintManager = new JFingerprintManager(this, KEY);
        fingerPrintManager.setAuthenticationDialogStyle(R.style.DialogThemeLight);
    }

    private void initClickListeners() {

        findViewById(R.id.buttonDialogThemeLight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectView(v);

                deselectView(findViewById(R.id.buttonDialogThemeDark));

                fingerPrintManager.setAuthenticationDialogStyle(R.style.DialogThemeLight);
            }
        });

        findViewById(R.id.buttonDialogThemeDark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectView(v);

                deselectView(findViewById(R.id.buttonDialogThemeLight));

                fingerPrintManager.setAuthenticationDialogStyle(R.style.DialogThemeDark);
            }
        });

        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fingerPrintManager.startAuthentication(new JFingerprintManager.AuthenticationCallback() {
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
                }, getSupportFragmentManager());
            }
        });

        encryptTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageToBeEncrypted = messageToBeEncryptedEditText.getText().toString();
                fingerPrintManager.encrypt(messageToBeEncrypted, new JFingerprintManager.EncryptionCallback() {
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
                    }

                    @Override
                    public void onEncryptionFailed() {
                        messageTextView.setText("Encryption failed");
                    }

                }, getSupportFragmentManager());
            }
        });
    }

    private void selectView(View view)
    {
        view.setSelected(true);
        view.setElevation(32);
    }

    private void deselectView(View view)
    {
        view.setSelected(false);
        view.setElevation(0);
    }
}

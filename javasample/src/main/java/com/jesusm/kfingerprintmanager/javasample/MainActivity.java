package com.jesusm.kfingerprintmanager.javasample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jesusm.kfingerprintmanager.KFingerprintManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity {

    private static final String KEY = "KEY";
    private int dialogTheme;
    private TextView messageText;
    private EditText encryptionMessageEditText;
    private Button encryptButton;
    private Button decryptButton;
    private String messageToDecrypt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectView(findViewById(R.id.buttonDialogThemeLight));
        messageText = (TextView) findViewById(R.id.message);
        encryptionMessageEditText = (EditText) findViewById(R.id.editText);
        encryptButton = (Button) findViewById(R.id.buttonEncrypt);
        decryptButton = (Button) findViewById(R.id.buttonDecrypt);

        initClickListeners();
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

        findViewById(R.id.buttonAuthenticate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFingerprintManagerInstance().authenticate(new KFingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        messageText.setText("Successfully authenticated");
                    }

                    @Override
                    public void onSuccessWithManualPassword(@NotNull String password) {
                        messageText.setText("Manual password: " + password);
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        messageText.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(@Nullable String help) {
                        messageText.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageText.setText("Fingerprint not available");
                    }

                    @Override
                    public void onCancelled() {
                        messageText.setText("Operation cancelled by user");
                    }
                }, getSupportFragmentManager());
            }
        });

        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageToDecrypt = encryptionMessageEditText.getText().toString();
                createFingerprintManagerInstance().encrypt(messageToDecrypt, new KFingerprintManager.EncryptionCallback() {
                    @Override
                    public void onEncryptionSuccess(@NotNull String messageEncrypted) {
                        String message = getString(R.string.encrypt_message_success, messageEncrypted);
                        messageText.setText(message);
                        encryptionMessageEditText.setText(messageEncrypted);
                        encryptButton.setVisibility(View.GONE);
                        decryptButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onEncryptionFailed() {
                        messageText.setText("Encryption failed");
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        messageText.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(@Nullable String help) {
                        messageText.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageText.setText("Fingerprint not available");
                    }

                    @Override
                    public void onCancelled() {
                        messageText.setText("Operation cancelled by user");
                    }
                }, getSupportFragmentManager());
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageToDecrypt = encryptionMessageEditText.getText().toString();
                createFingerprintManagerInstance().decrypt(messageToDecrypt, new KFingerprintManager.DecryptionCallback() {
                    @Override
                    public void onDecryptionSuccess(@NotNull String messageDecrypted) {
                        String message = getString(R.string.decrypt_message_success, messageDecrypted);
                        messageText.setText(message);
                        encryptionMessageEditText.setText("");
                        decryptButton.setVisibility(View.GONE);
                        encryptButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onDecryptionFailed() {
                        messageText.setText("Encryption failed");
                    }

                    @Override
                    public void onFingerprintNotRecognized() {
                        messageText.setText("Fingerprint not recognized");
                    }

                    @Override
                    public void onAuthenticationFailedWithHelp(@Nullable String help) {
                        messageText.setText(help);
                    }

                    @Override
                    public void onFingerprintNotAvailable() {
                        messageText.setText("Fingerprint not available");
                    }

                    @Override
                    public void onCancelled() {
                        messageText.setText("Operation cancelled by user");
                    }
                }, getSupportFragmentManager());
            }
        });
    }

    private KFingerprintManager createFingerprintManagerInstance() {
        KFingerprintManager fingerprintManager = new KFingerprintManager(this, KEY);
        fingerprintManager.setAuthenticationDialogStyle(dialogTheme);
        return fingerprintManager;
    }

    private void selectView(View view) {
        view.setSelected(true);
        view.setElevation(32f);
    }

    private void deselectView(View view) {
        view.setSelected(true);
        view.setElevation(0f);
    }
}

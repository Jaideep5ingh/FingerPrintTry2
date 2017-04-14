package com.component.independent.fingerprinttry2;

import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private SecretKey key;
    private static final String KEY_NAME = "sweet";
    private Cipher cipher;
    private TextView textView;
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
    private FingerprintManager.CryptoObject cryptoObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        textView = (TextView) findViewById(R.id.textView);

        if (!fingerprintManager.isHardwareDetected()) {
            textView.setText("Finger Print Hardware not found");
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.USE_FINGERPRINT)
                    != PackageManager.PERMISSION_GRANTED) {
                textView.setText
                        ("Fingerprint Authentication requires permission");
            } else {

                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    textView.setText("You need atleast one fingerprint");
                } else {
                    if (!keyguardManager.isKeyguardSecure()) {
                        textView.setText
                                ("Lock screen not enabled in settings");
                    } else {

                        generateKey();

                        if (cipherInit()) {
                            cryptoObject =
                                    new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);

                        }
                    }
                }
            }
        }
    }

    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

        } catch (Exception e) {
            e.printStackTrace();
        }

        keyGenerator = null;
        try {
            keyGenerator =
                    KeyGenerator.getInstance
                            (KeyProperties.KEY_ALGORITHM_AES,
                                    "AndroidKeyStore");

        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(this, "Error on line 100", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            Toast.makeText(this, "Error on line 103", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder
                    (KEY_NAME, KeyProperties.PURPOSE_ENCRYPT
                            | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings
                            (KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();


        } catch (CertificateException |
                InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean cipherInit() {
        try {
            cipher = Cipher.getInstance
                    (KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);

        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed To get Cipher ", e);
        }


        try {
            keyStore.load(null);
            key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (IOException | CertificateException| NoSuchAlgorithmException|UnrecoverableKeyException
                |InvalidKeyException |KeyStoreException e) {
            throw  new RuntimeException("Failed to init Cipher",e);
        }
    }
}

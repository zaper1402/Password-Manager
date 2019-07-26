package com.example.passwordmanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email, pass;
    Button login;
    TextView register;
    FirebaseAuth mAuth;
    private FirebaseAnalytics firebaseAnalytics;
    String regEmail, regPassword;
    TextInputLayout textInputLayout;
    boolean backPressExitFlag = false;

    private KeyStore keyStore;
    private static final String KEY_NAME = "androidHive";
    private Cipher cipher;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        textInputLayout = findViewById(R.id.textinputpass);
        mAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        pass.setOnClickListener(this);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if(!fingerprintManager.isHardwareDetected()){
            Toast.makeText(getApplicationContext(),"Your Device does not have a Fingerprint Sensor",Toast.LENGTH_SHORT);
        }else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"Fingerprint authentication permission not enabled",Toast.LENGTH_SHORT);
            }else{
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Toast.makeText(getApplicationContext(),"Register at least one fingerprint in Settings",Toast.LENGTH_SHORT);
                }else{
                    if (!keyguardManager.isKeyguardSecure()) {
                        Toast.makeText(getApplicationContext(),"Lock screen security not enabled in Settings",Toast.LENGTH_SHORT);
                    }else{
                        generateKey();

                        if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
                break;

            case R.id.login:
                regEmail = email.getText().toString();
                regPassword = pass.getText().toString();

                if (checkEmail(regEmail) && checkPassword(regPassword)) {
                    signInUser(regEmail, regPassword);
                }
                break;
            case R.id.password:
                textInputLayout.setPasswordVisibilityToggleEnabled(true);
                break;


        }
    }

    @Override
    public void onBackPressed() {
        if (backPressExitFlag) {
            finishAffinity();
            return;
        }

        this.backPressExitFlag = true;
        Toast.makeText(this, "Press Back Button again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressExitFlag = false;
            }
        }, 2000);
    }

    private void signInUser(String regEmail, final String regPassword) {



        mAuth.signInWithEmailAndPassword(regEmail, regPassword)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.putExtra("USER_PASSWORD", regPassword);
                            startActivity(i);
                            finish();

//                            if (mAuth.getCurrentUser().isEmailVerified()) {
//                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                                i.putExtra("USER_PASSWORD", regPassword);
//                                startActivity(i);
//                                finish();
//                            }
//                            else {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
//                                AlertDialog alertDialog;
//                                builder.setTitle("Email not verified.")
//                                        .setMessage("Verify Email now?")
//                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                mAuth.getCurrentUser().sendEmailVerification();
//                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
//                                                AlertDialog alertDialog1;
//                                                builder1.setMessage("Email Verification Link sent!")
//                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                            @Override
//                                                            public void onClick(DialogInterface dialog, int which) {
//                                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                                                                i.putExtra("USER_PASSWORD", regPassword);
//                                                                startActivity(i);
//                                                                finish();
//                                                            }
//                                                        });
//                                                alertDialog1 = builder1.create();
//                                                alertDialog1.show();
//                                            }
//                                        })
//                                        .setNegativeButton("Later", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                                                i.putExtra("USER_PASSWORD", regPassword);
//                                                startActivity(i);
//                                                finish();
//                                            }
//                                        });
//                                alertDialog = builder.create();
//                                alertDialog.show();
//                            }``
                            String regemail = email.getText().toString();
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.METHOD, regemail );
                            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Wrong Username\nor Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean checkPassword(String regPassword) {
        if (regPassword.isEmpty()) {
            pass.setError("Password is required!");
            pass.requestFocus();
            textInputLayout.setPasswordVisibilityToggleEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkEmail(String regEmail) {
        if (regEmail.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
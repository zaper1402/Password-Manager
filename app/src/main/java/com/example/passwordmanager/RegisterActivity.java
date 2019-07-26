package com.example.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email;
    EditText password;
    Button register;
    TextView login;
    String newEmail;
    String newPassword;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    TextInputLayout textInputLayout;
    FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        login = findViewById(R.id.alreadyreg);
        textInputLayout = findViewById(R.id.textinputpass);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        register.setOnClickListener(this);
        login.setOnClickListener(this);
        password.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alreadyreg:
                Intent logInActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(logInActivity);
                break;

            case R.id.register:
                newEmail = email.getText().toString();
                newPassword = password.getText().toString();
                if (checkEmailFormat(newEmail) && checkPasswordFormat(newPassword)) {
                    registerUser(newEmail, newPassword);
                }
            case R.id.password:
                textInputLayout.setPasswordVisibilityToggleEnabled(true);
                break;
        }
    }

    public void registerUser(final String newEmail, final String newPassword) {
        firebaseAuth.createUserWithEmailAndPassword(newEmail, newPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    AlertDialog alertDialog;
                    builder.setMessage("An Email verification link has been sent to your Email address.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent mainActivity = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainActivity.putExtra("USER_PASSWORD", newPassword);
                                    startActivity(mainActivity);
                                }
                            });
                    alertDialog = builder.create();
                    alertDialog.show();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.METHOD, newEmail);
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
                } else {
                    Toast.makeText(RegisterActivity.this, "Error: \n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Boolean checkEmailFormat(String newEmail) {
        if (newEmail.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            return false;
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                return true;
            } else {
                email.setError("Invalid Email!");
                email.requestFocus();
                return false;
            }

        }
    }

    public Boolean checkPasswordFormat(String newPassword) {
        if (newPassword.isEmpty()) {
            textInputLayout.setPasswordVisibilityToggleEnabled(false);
            password.setError("Password is required!");
            password.requestFocus();
            return false;
        } else {
            if (newPassword.length() < 6) {
                password.setError("Password must be 6 characters long");
                password.requestFocus();
                return false;
            } else {
                return true;
            }
        }
    }
}
package com.usa.passwordmanager;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.crypto.SecretKey;

public class Add_Detail extends AppCompatActivity {

    EditText editTextTitle, editTextUserName, editTextPassword,editTextComment;
    Button submitButton;
    String title, userName, password,commentText;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    String currentUserID,userPassword;
    boolean exist = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__detail);
        editTextTitle = findViewById(R.id.title);
        editTextUserName = findViewById(R.id.userName);
        editTextPassword = findViewById(R.id.password);
        editTextComment = findViewById(R.id.commentTxt);
        submitButton = findViewById(R.id.submitButton);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userPassword = getIntent().getStringExtra("USER_PASSWORD");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);

        editTextComment.setScroller(new Scroller(this));
        editTextComment.setMaxLines(4);
        editTextComment.setVerticalScrollBarEnabled(true);
        editTextComment.setMovementMethod(new ScrollingMovementMethod());


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = editTextTitle.getText().toString();
                userName = editTextUserName.getText().toString();
                password = editTextPassword.getText().toString();
                commentText = ""+editTextComment.getText().toString();


                if (ifEmpty(title, userName, password,commentText) == true) {
                    try {
                        submitDetail(title, userName, password,commentText, databaseReference);
                    } catch (Exception e) {
                        Toast.makeText(Add_Detail.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    editTextTitle.setText("");
                    editTextUserName.setText("");
                    editTextPassword.setText("");
                    editTextComment.setText("");
                }
            }
        });
    }

    private boolean ifEmpty(String title, String userName, String password,String commentText) {
        if (title.isEmpty()) {
            editTextTitle.setError("Please Enter A Title");
            editTextTitle.requestFocus();
            return false;
        } else if (userName.isEmpty()) {
            editTextUserName.setError("Please Enter A UserName");
            editTextUserName.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            editTextPassword.setError("Please Enter A Password");
            editTextPassword.requestFocus();
            return false;
        } else if (commentText.isEmpty()) {
            return true;
        }else {
            return true;
        }
    }

    private void submitDetail(final String title, final String userName, final String password,final String commentText, final DatabaseReference databaseReference) throws Exception {
        Encryption encryption = new Encryption();
        SecretKey secretKey = encryption.generateKey(userPassword);
        final String encryptPassword = encryption.encryptData(password, secretKey);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Detail newDetail = dataSnapshot1.getValue(Detail.class);
                    if (newDetail.getUserName().equals(userName) && newDetail.getPassword().equals(encryptPassword)) {
                    Toast.makeText(Add_Detail.this, "Data already exists.", Toast.LENGTH_SHORT).show();
                    exist = true;
                }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(exist==false){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 10000);
                    String key = databaseReference.push().getKey();
                    databaseReference.child(key).setValue(new Detail(title, userName, encryptPassword,commentText)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Add_Detail.this, "Your details have been saved.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }, 10000);


    }



}
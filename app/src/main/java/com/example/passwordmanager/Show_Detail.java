package com.example.passwordmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Show_Detail extends AppCompatActivity implements View.OnClickListener {

    TextView textViewTitle, textViewUserName, textViewPassword,textViewComment;
    EditText editTextTitle, editTextUserName, editTextPassword,editTextComment;

    ViewSwitcher switcherTitle, switcherUserName, switcherPassword,switcherComment;

    Button save, edit, delete, cancel;
    Button viewPassword;

    String title, userName, password,comment;
    String newTitle, newUsername, newPassword,newComment;
    String encryptPassword;
    String newEncryptPassword;
    String userPassword;


    AlertDialog alertDialog;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    private boolean passwordIsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__detail);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewPassword = findViewById(R.id.textViewPassword);
        textViewComment = findViewById(R.id.textViewComment);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextUserName = findViewById(R.id.editTextUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextComment = findViewById(R.id.edittextComment);

        switcherTitle = findViewById(R.id.my_switcher);
        switcherUserName = findViewById(R.id.my_switcher2);
        switcherPassword = findViewById(R.id.my_switcher3);
        switcherComment = findViewById(R.id.my_switcher4);

        edit = findViewById(R.id.edit);
        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);
        delete = findViewById(R.id.delete);
        viewPassword = findViewById(R.id.viewpassword);

        editTextComment.setScroller(new Scroller(this));
        editTextComment.setMaxLines(4);
        editTextComment.setVerticalScrollBarEnabled(true);
        editTextComment.setMovementMethod(new ScrollingMovementMethod());

        title = getIntent().getStringExtra("TITLE");
        userName = getIntent().getStringExtra("USERNAME");
        encryptPassword = getIntent().getStringExtra("PASSWORD");
        userPassword = getIntent().getStringExtra("USER_PASSWORD");
        comment = getIntent().getStringExtra("COMMENT");

        try {
            password = new Encryption().decryptData(encryptPassword, new Encryption().generateKey(userPassword));
        } catch (Exception e) {
        }

        textViewTitle.setText("" + title);
        textViewUserName.setText("" + userName);
        textViewPassword.setText("" + password);
        textViewComment.setText(""+comment);

        firebaseAuth = FirebaseAuth.getInstance();
        String userID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        viewPassword.setOnClickListener(this);

        edit.setOnClickListener(this);
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.viewpassword:

                passwordIsShown = passwordIsShown ? false : true;
                if (passwordIsShown) {
                    textViewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    textViewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

                break;


            case R.id.edit:

                switcherTitle.showNext();
                switcherUserName.showNext();
                switcherPassword.showNext();
                switcherComment.showNext();

                editTextTitle.setText("" + title);
                editTextUserName.setText("" + userName);
                editTextPassword.setText("" + password);
                editTextComment.setText(""+ comment);

                textViewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordIsShown = false;
                viewPassword.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);

                break;

            case R.id.save:

                newTitle = editTextTitle.getText().toString();
                newUsername = editTextUserName.getText().toString();
                newPassword = editTextPassword.getText().toString();
                newComment = editTextComment.getText().toString();

                if (!ifEmpty(newTitle, newUsername, newPassword)) {
                    if (ifChanged(new Detail(newTitle, newUsername, newPassword,newComment),
                            new Detail(title, userName, password,comment))) {

                        try {
                            newEncryptPassword = new Encryption().encryptData(newPassword, new Encryption().generateKey(userPassword));
                        } catch (Exception e) {
                            Toast.makeText(this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    if (matchDetail(dataSnapshot1.getValue(Detail.class))) {
                                        dataSnapshot1.getRef()
                                                .setValue(new Detail(newTitle, newUsername, newEncryptPassword,newComment))
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            title = newTitle;
                                                            userName = newUsername;
                                                            password = newPassword;
                                                            encryptPassword = newEncryptPassword;
                                                            comment = newComment;

                                                            textViewTitle.setText("" + title);
                                                            textViewUserName.setText("" + userName);
                                                            textViewPassword.setText("" + password);
                                                            textViewComment.setText(""+ comment);

                                                            Toast.makeText(Show_Detail.this, "Changes Saved", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Show_Detail.this, "Error:\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    }

                    switcherTitle.showPrevious();
                    switcherUserName.showPrevious();
                    switcherPassword.showPrevious();
                    switcherComment.showPrevious();

                    viewPassword.setVisibility(View.VISIBLE);
                    save.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    edit.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                }

                break;

            case R.id.cancel:

                switcherTitle.showPrevious();
                switcherUserName.showPrevious();
                switcherPassword.showPrevious();
                switcherComment.showPrevious();

                viewPassword.setVisibility(View.VISIBLE);
                save.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);

                break;

            case R.id.delete:

                final AlertDialog.Builder builder = new AlertDialog.Builder(Show_Detail.this);
                builder.setMessage("Are you sure you want to delete this detail?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            if (matchDetail(dataSnapshot1.getValue(Detail.class))) {
                                                dataSnapshot1.getRef()
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Show_Detail.this, "Details deleted.", Toast.LENGTH_SHORT).show();
                                                                    Intent mainActivity = new Intent(Show_Detail.this, MainActivity.class);
                                                                    mainActivity.putExtra("USER_PASSWORD", userPassword);
                                                                    startActivity(mainActivity);
                                                                } else {
                                                                    Toast.makeText(Show_Detail.this, "Error:\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });

                alertDialog = builder.create();
                alertDialog.show();

                break;
        }
    }

    private boolean ifEmpty(String title, String userName, String password) {
        if (title.isEmpty()) {
            editTextTitle.setError("Enter a Title");
            editTextTitle.requestFocus();
            return true;
        } else if (userName.isEmpty()) {
            editTextUserName.setError("Enter a Username");
            editTextUserName.requestFocus();
            return true;
        } else if (password.isEmpty()) {
            editTextPassword.setError("Enter a Password");
            editTextPassword.requestFocus();
            return true;
        } else {
            return false;
        }
    }

    private boolean ifChanged(Detail newDetail, Detail oldDetail) {
        boolean changedTitle = !(newDetail.getTitle().equals(oldDetail.getTitle()));
        boolean changedUserName = !(newDetail.getUserName().equals(oldDetail.getUserName()));
        boolean changedPassword = !(newDetail.getPassword().equals(oldDetail.getPassword()));
        boolean changedComment = !(newDetail.getComment().equals(oldDetail.getComment()));


        if (changedTitle || changedUserName || changedPassword || changedComment) {
            return true;
        } else {
            Toast.makeText(this, "No changes Made!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean matchDetail(Detail detail) {
        if (
                detail.getTitle().equals(title) &&
                        detail.getUserName().equals(userName) &&
                        detail.getPassword().equals(encryptPassword)&&
                        detail.getComment().equals(comment)
        ) {
            return true;
        }
        return false;
    }
}
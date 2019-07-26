package com.example.passwordmanager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProfileDetail extends AppCompatActivity implements View.OnClickListener {
    TextView textViewName, textViewAge;
    EditText editTextName, editTextAge;
    ArrayList<UserInfo> userList = new ArrayList<>();
    ArrayList<UserInfo> duplicateUserList = new ArrayList<>();
    ViewSwitcher switcherName, switcherAge;
    RadioGroup radioGroupGender;
    RadioButton radioButtonGender;
    Button edit,save;

    String name,gender,age;
    String newName,newGender,newAge;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        textViewAge = findViewById(R.id.textViewAge);
        textViewName = findViewById(R.id.textViewName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextName = findViewById(R.id.editTextName);

        switcherName = findViewById(R.id.my_switcher);
        switcherAge = findViewById(R.id.my_switcher2);
        radioGroupGender = findViewById(R.id.radioGroupGender);

        edit = findViewById(R.id.edit);
        save = findViewById(R.id.save);

        edit.setOnClickListener(this);
        save.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        String userID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("usersDetails").child(userID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    UserInfo newUserInfo = dataSnapshot.getValue(UserInfo.class);
                    textViewAge.setText(newUserInfo.age);
                    textViewName.setText(newUserInfo.name);
                    switch (newUserInfo.gender.toString()){
                        case "Male":
                           radioButtonGender = findViewById(R.id.radioButtonMale);
                           radioButtonGender.setChecked(true);
                           break;
                        case "Female":
                            radioButtonGender = findViewById(R.id.radioButtonFemale);
                            radioButtonGender.setChecked(true);
                            break;
                        case "Other":
                            radioButtonGender = findViewById(R.id.radioButtonOther);
                            radioButtonGender.setChecked(true);
                            break;


                    }

            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:

                switcherName.showNext();
                switcherAge.showNext();

                save.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                        name= userInfo.getName();
                        age = userInfo.getAge();
                        gender = userInfo.getGender();

                        editTextAge.setText(age);
                        editTextName.setText(name);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                    }
                });
                break;
            case R.id.save:
                newName= editTextName.getText().toString();
                 int selectedId = radioGroupGender.getCheckedRadioButtonId();
                radioButtonGender = (RadioButton) findViewById(selectedId);
                newGender = radioButtonGender.getText().toString();
                newAge = editTextAge.getText().toString();
                databaseReference.setValue(new UserInfo(newName,newGender,newAge)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileDetail.this, "Your details have been saved.", Toast.LENGTH_SHORT).show();
                    }
                });
                switcherName.showPrevious();
                switcherAge.showPrevious();

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
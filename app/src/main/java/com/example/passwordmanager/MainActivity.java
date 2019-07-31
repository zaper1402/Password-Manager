package com.example.passwordmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton addButton;
    ListView listView;
    EditText searchField;
    ImageView alphabets;
    DatabaseReference databaseReference;
    ArrayList<Detail> detailsList = new ArrayList<>();
    ArrayList<Detail> duplicateDetailsList = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    String currentUserID,userPassword;
    boolean backPressExitFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        listView = findViewById(R.id.listView);
        final ArrayAdapter<Detail> adapter = new MyAdapter(this, 0, detailsList);
        listView.setAdapter(adapter);
        searchField = findViewById(R.id.heading);
        alphabets = findViewById(R.id.albhabets);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userPassword = getIntent().getStringExtra("USER_PASSWORD");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Detail> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Detail newDetail = dataSnapshot1.getValue(Detail.class);
                    list.add(newDetail);
                }
                detailsList.clear();
                Collections.sort(
                        list,
                        new Comparator<Detail>() {
                            public int compare(Detail detail1, Detail detail2) {
                                return detail1.title.compareToIgnoreCase(detail2.title);
                            }
                        }
                );
                detailsList.addAll(list);
                adapter.notifyDataSetChanged();
                duplicateDetailsList.clear();
                duplicateDetailsList.addAll(detailsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Detail clickedDetail = (Detail) parent.getAdapter().getItem(position);
                Intent showDetail = new Intent(MainActivity.this, Show_Detail.class);
                showDetail.putExtra("TITLE", clickedDetail.getTitle());
                showDetail.putExtra("USERNAME", clickedDetail.getUserName());
                showDetail.putExtra("PASSWORD", clickedDetail.getPassword());
                showDetail.putExtra("USER_PASSWORD", userPassword);
                showDetail.putExtra("COMMENT",clickedDetail.getComment());
                startActivity(showDetail);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addDetail = new Intent(MainActivity.this, Add_Detail.class);
                addDetail.putExtra("USER_PASSWORD", userPassword);
                startActivity(addDetail);
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchKeyword = searchField.getText().toString().toLowerCase();
                ArrayList<Detail> filterList = new ArrayList<>();
                detailsList.clear();
                detailsList.addAll(duplicateDetailsList);
                for (int i = 0; i < duplicateDetailsList.size(); i++) {
                    if (detailsList.get(i).getTitle().toLowerCase().contains(searchKeyword) || detailsList.get(i).getUserName().toLowerCase().contains(searchKeyword)) {
                        Detail filteredDetail = detailsList.get(i);
                        filterList.add(filteredDetail);
                    }
                }
                detailsList.clear();
                detailsList.addAll(filterList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_actionbar, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent profile = new Intent(MainActivity.this, ProfileDetail.class);
                startActivity(profile);
                return true;
            case R.id.Credits:
                Intent credit = new Intent(MainActivity.this, CreditInfo.class);
                startActivity(credit);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressExitFlag) {
            finishAffinity();
            return;
        }

        this.backPressExitFlag = true;
        Toast.makeText(this, "Press Back button again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressExitFlag = false;
            }
        }, 1500);
    }

    private class MyAdapter extends ArrayAdapter<Detail> {
        Context context;
        ArrayList<Detail> detailsList;
        TextView title;
        TextView userName;

        public MyAdapter(Context context, int resource, ArrayList<Detail> detailsList) {
            super(context, resource, detailsList);
            this.detailsList = detailsList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return detailsList.size();
        }

        @Override
        public Detail getItem(int position) {
            return detailsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RecyclerView.ViewHolder holder;
            Detail currentDetail = detailsList.get(position);

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.single_detail, null);

            alphabets = view.findViewById(R.id.albhabets);
            title = view.findViewById(R.id.detailTitle);
            userName = view.findViewById(R.id.userName);
            title.setText("" + currentDetail.getTitle());
            userName.setText("" + currentDetail.getUserName());

            if(detailsList.get(position).getTitle().toLowerCase().contains("gmail")){
                alphabets.setImageResource(R.drawable.gmail);
            }else if (detailsList.get(position).getTitle().toLowerCase().contains("facebook")){
                alphabets.setImageResource(R.drawable.facebook);
            }else if (detailsList.get(position).getTitle().toLowerCase().contains("twitter")){
                alphabets.setImageResource(R.drawable.twitter);
            }else if (detailsList.get(position).getTitle().toLowerCase().contains("instagram")){
                alphabets.setImageResource(R.drawable.instagram);
            }else if (detailsList.get(position).getTitle().toLowerCase().contains("snapchat")){
                alphabets.setImageResource(R.drawable.snapchat);
            }else if (detailsList.get(position).getTitle().toLowerCase().contains("linkedin")){
                alphabets.setImageResource(R.drawable.linkedin);
            }
            else {
                String firstLetter = String.valueOf(detailsList.get(position).getTitle().charAt(0));
                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(getItem(position));
                TextDrawable drawable = TextDrawable.builder().buildRound(firstLetter, color);
                alphabets.setImageDrawable(drawable);
            }
            return view;
        }
    }

}
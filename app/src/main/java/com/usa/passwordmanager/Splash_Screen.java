package com.usa.passwordmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash_Screen extends AppCompatActivity {
    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash__screen);

        start();
    }

    private void start() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isConnected()) {
                    startActivity(new Intent(Splash_Screen.this, LoginActivity.class));
                    finish();
                } else {
                    builder = new AlertDialog.Builder(Splash_Screen.this);
                    builder.setCancelable(false)
                            .setTitle("Connection Error")
                            .setMessage("Looks like you're not connected to Internet. Check you connection and try again!");

                    builder.setNegativeButton(
                            "Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });

                    builder.setPositiveButton("Retry",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (isConnected()) {
                                        alertDialog.dismiss();
                                        start();
                                    } else {
                                        builder.show();
                                    }
                                }
                            });

                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }, 3000);
    }

    public boolean isConnected() {
        NetworkInfo activeNetwork = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
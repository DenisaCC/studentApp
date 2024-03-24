package com.example.licentaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private static Button button_sbm1, button_sbm2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        OnClickButton1Listener();
        OnClickButton2Listener();
    }

    public void OnClickButton1Listener() {
        button_sbm1 = (Button) findViewById(R.id.button1);
        button_sbm1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }

    public void OnClickButton2Listener() {
        button_sbm2 = (Button) findViewById(R.id.button2);
        button_sbm2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
        );

    }
}
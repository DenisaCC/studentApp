package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CampusActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_campus);

        Intent intent = getIntent();
        if (intent.hasExtra("USERNAME")) {
            username = intent.getStringExtra("USERNAME");
        }
        
        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CampusActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        ImageButton canteenButton = findViewById(R.id.canteenButton);
        canteenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CampusActivity.this, CanteenActivity.class);
                startActivity(intent);
            }
        });

        ImageButton caminButton = findViewById(R.id.caminButton);
        caminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CampusActivity.this, CaminActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        ImageButton buildingsButton = findViewById(R.id.buildingsButton);
        buildingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CampusActivity.this, FacultyBuildingActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }
}
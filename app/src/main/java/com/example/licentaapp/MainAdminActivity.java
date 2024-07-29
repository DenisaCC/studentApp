package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);


       ImageButton logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAdminActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(MainAdminActivity.this, "V-ați deconectat de la contul de administrator!", Toast.LENGTH_SHORT).show();

            }
        });

        Button chatFunctionalityButton = findViewById(R.id.chatFunctionalityButton);
        chatFunctionalityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschide o nouă activitate sau fragment pentru sincronizarea bazelor de date
                Intent intent = new Intent(MainAdminActivity.this, SyncDatabaseActivity.class);
                startActivity(intent);
            }
        });

        Button updateTaxesButton = findViewById(R.id.updateTaxesButton);
        updateTaxesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadTaxesActivity.class);
                startActivity(intent);
            }
        });

        Button uploadProfProgram = findViewById(R.id.uploadProfProgram);
        uploadProfProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadProfProgramActivity.class);
                startActivity(intent);
            }
        });

        Button insertStudents = findViewById(R.id.insertStudents);
        insertStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, InsertStudentsActivity.class);
                startActivity(intent);
            }
        });

        Button insertProfessors = findViewById(R.id.insertProfessors);
        insertProfessors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, InsertProfessorsActivity.class);
                startActivity(intent);
            }
        });

        Button manageProfessors = findViewById(R.id.manageProfessors);
        manageProfessors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, ManageProfessorsActivity.class);
                startActivity(intent);
            }
        });

        Button manageStudents = findViewById(R.id.manageStudents);
        manageStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, ManageStudentsActivity.class);
                startActivity(intent);
            }
        });

        Button uploadSchedule = findViewById(R.id.uploadSchedule);
        uploadSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadScheduleActivity.class);
                startActivity(intent);
            }
        });

        Button uploadCourses = findViewById(R.id.uploadCourses);
        uploadCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadCoursesActivity.class);
                startActivity(intent);
            }
        });

        Button uploadRules = findViewById(R.id.uploadRules);
        uploadRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadRulesActivity.class);
                startActivity(intent);
            }
        });

        Button uploadMenu = findViewById(R.id.uploadMenu);
        uploadMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, UploadMenuActivity.class);
                startActivity(intent);
            }
        });

        Button uploadNews = findViewById(R.id.uploadNews);
        uploadNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lansați activitatea de încărcare a fișierului Excel
                Intent intent = new Intent(MainAdminActivity.this, AddNewsActivity.class);
                startActivity(intent);
            }
        });
    }
}
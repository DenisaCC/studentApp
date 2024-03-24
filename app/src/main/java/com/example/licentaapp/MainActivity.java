package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        TextView textUsername = headerView.findViewById(R.id.textUsername);

        // Setează numele de utilizator în TextView
        textUsername.setText(username);

        ImageView useImage = headerView.findViewById(R.id.userImage);

        useImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AccountDetailsActivity.class);
                intent.putExtra("USERNAME", textUsername.getText().toString());
                startActivity(intent);
            }
        });

        textUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Deschideți o altă activitate pentru a afișa detaliile contului și trimiteți numele de utilizator
                Intent intent = new Intent(MainActivity.this, AccountDetailsActivity.class);
                intent.putExtra("USERNAME", textUsername.getText().toString());
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == R.id.navNews){
                    Toast.makeText(MainActivity.this, "News clicked", Toast.LENGTH_SHORT).show();
                }
                if(itemId == R.id.navAnnounce){
                    Toast.makeText(MainActivity.this, "Announce clicked", Toast.LENGTH_SHORT).show();
                }
                if(itemId == R.id.navSettings){
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    intent.putExtra("USERNAME", textUsername.getText().toString());
                    startActivity(intent);
                }
                if(itemId == R.id.navLogout){
                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(MainActivity.this, "V-ați deconectat de la contul dumneavoastră!", Toast.LENGTH_SHORT).show();
                }
                drawerLayout.close();
                return false;
            }
        });
    }
}

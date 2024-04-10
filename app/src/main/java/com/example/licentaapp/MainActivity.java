package com.example.licentaapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;
    private ImageButton coursesBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coursesBtn = findViewById(R.id.coursesBtn);

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

        coursesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeekSelectionDialog(username);
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

    private void showWeekSelectionDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Selectați săptămâna");

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_week_selection, null);
        builder.setView(dialogView);

        //dialogView.setBackgroundResource(R.drawable.white_background);

        Spinner spinnerWeek = dialogView.findViewById(R.id.weekSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.week_parity_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeek.setAdapter(adapter);

        // Set the positive button to handle selection
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedWeek = spinnerWeek.getSelectedItem().toString();
                // Handle the selected week here
                Intent intent;
                if (selectedWeek.equals("Săptămâna pară")) {
                    // Start the activity for even week
                    intent = new Intent(MainActivity.this, EvenWeekActivity.class);
                } else {
                    // Start the activity for odd week
                    intent = new Intent(MainActivity.this, OddWeekActivity.class);
                }
                intent.putExtra("USERNAME", username); // Trimiteți numele de utilizator către activitatea corespunzătoare
                startActivity(intent);
            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("Anulare", null);

        // Display the dialog
        builder.create().show();
    }
}

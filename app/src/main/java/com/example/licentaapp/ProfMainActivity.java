package com.example.licentaapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ProfMainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;

    private String username, password;
    private int professorId;
    private ImageButton programBtn, gradesBtn, chatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);
        chatBtn = findViewById(R.id.chatBtn);
        programBtn = findViewById(R.id.programBtn);
        gradesBtn = findViewById(R.id.gradesBtn);

        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navNews) {
                startActivityWithUsername(NewsActivity.class);
            } else if (itemId == R.id.navSettings) {
                startActivityWithUsername(SettingsActivity.class);
            } else if (itemId == R.id.navRules) {
                startActivityWithUsername(RulesActivity.class);
            } else if (itemId == R.id.navLogout) {
                Intent logoutIntent = new Intent(ProfMainActivity.this, StartActivity.class);
                startActivity(logoutIntent);
                finish();
                Toast.makeText(ProfMainActivity.this, "V-ați deconectat de la contul dumneavoastră!", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView textUsername = headerView.findViewById(R.id.textUsername);

        // Preia username-ul și professorId din Intent și setează-l în Navigation Drawer
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        professorId = intent.getIntExtra("PROFESSOR_ID", -1);
        password = intent.getStringExtra("PASSWORD");

        if (username != null) {
            textUsername.setText(username);
        }
        Log.d("ProfMainActivity", "Nume utilizator profesor: " + username);
        Log.d("ProfMainActivity", "ID profesor: " + professorId);
        Log.d("ProfMainActivity", "parola: " + password);
        ImageView userImage = headerView.findViewById(R.id.userImage);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfMainActivity.this, ProfAccountDetailsActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("PROFESSOR_ID", professorId);
                startActivity(intent);
            }
        });

        textUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfMainActivity.this, ProfAccountDetailsActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("PROFESSOR_ID", professorId);
                startActivity(intent);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username != null) {
                    Intent intent = new Intent(ProfMainActivity.this, ProfChatMainActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("PROFESSOR_ID", professorId);
                    intent.putExtra("PASSWORD",password);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfMainActivity.this, "Eroare: Numele utilizatorului este null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        programBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeekSelectionDialog();
            }
        });

        gradesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username != null) {
                    Intent intent = new Intent(ProfMainActivity.this, InsertGradeActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("PROFESSOR_ID", professorId);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfMainActivity.this, "Eroare: Numele utilizatorului este null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.navSettings) {
                    Intent intent = new Intent(ProfMainActivity.this, SettingsActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("PROFESSOR_ID", professorId);
                    startActivity(intent);
                }
                if (itemId == R.id.navLogout) {
                    Intent intent = new Intent(ProfMainActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(ProfMainActivity.this, "V-ați deconectat de la contul dumneavoastră", Toast.LENGTH_SHORT).show();
                }
                if (itemId == R.id.navRules) {
                    Intent intent = new Intent(ProfMainActivity.this, RulesActivity.class);
                    startActivity(intent);
                }
                drawerLayout.close();
                return false;
            }
        });
    }

    private void startActivityWithUsername(Class<?> cls) {
        Intent intent = new Intent(ProfMainActivity.this, cls);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    private void showWeekSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfMainActivity.this);
        builder.setTitle("Selectați săptămâna");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_prof_week_selection, null);
        builder.setView(dialogView);

        Spinner spinnerWeek = dialogView.findViewById(R.id.weekSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ProfMainActivity.this,
                R.array.week_parity_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeek.setAdapter(adapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedWeek = spinnerWeek.getSelectedItem().toString();
                Intent intent;
                if (selectedWeek.equals("Săptămâna pară")) {
                    intent = new Intent(ProfMainActivity.this, ProfEvenWeekActivity.class);
                } else {
                    intent = new Intent(ProfMainActivity.this, ProfOddWeekActivity.class);
                }
                intent.putExtra("USERNAME", username);
                intent.putExtra("PROFESSOR_ID", professorId);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Anulare", null);

        builder.create().show();
    }
}

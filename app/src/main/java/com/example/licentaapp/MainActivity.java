package com.example.licentaapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.licentaapp.Connection.ConnectionClass;
import com.google.android.material.navigation.NavigationView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private ImageView userImage;
    private String username, password;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private int nrMatricol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        drawerLayout = findViewById(R.id.drawerLayout);
        userImage = findViewById(R.id.userImage);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        password = intent.getStringExtra("PASSWORD");

        buttonDrawerToggle.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        TextView textUsername = headerView.findViewById(R.id.textUsername);
        textUsername.setText(username);

        textUsername.setOnClickListener(view -> {
            Intent accountDetailsIntent = new Intent(MainActivity.this, AccountDetailsActivity.class);
            accountDetailsIntent.putExtra("USERNAME", textUsername.getText().toString());
            startActivity(accountDetailsIntent);
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
                Intent logoutIntent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(logoutIntent);
                finish();
                Toast.makeText(MainActivity.this, "V-ați deconectat de la contul dumneavoastră!", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Verificare permisiuni pentru stocare
        checkStoragePermission();

        // Load number matricol
        new GetNrMatricolTask().execute(username);

        // Setare onClickListener pentru butoanele existente
        ImageButton coursesBtn = findViewById(R.id.coursesBtn);
        ImageButton facultyBtn = findViewById(R.id.facultyBtn);
        ImageButton gradesBtn = findViewById(R.id.gradesBtn);
        ImageButton campusBtn = findViewById(R.id.campusBtn);
        ImageButton taxesBtn = findViewById(R.id.taxeBtn);
        ImageButton chatBtn = findViewById(R.id.chatBtn);

        coursesBtn.setOnClickListener(v -> showWeekSelectionDialog(username));

        facultyBtn.setOnClickListener(v -> {
            Intent intentFaculty = new Intent(MainActivity.this, FacultyActivity.class);
            intentFaculty.putExtra("USERNAME", username);
            startActivity(intentFaculty);
        });

        gradesBtn.setOnClickListener(v -> {
            Intent intentGrades = new Intent(MainActivity.this, GradeActivity.class);
            intentGrades.putExtra("NR_MATRICOL", nrMatricol);
            startActivity(intentGrades);
        });

        campusBtn.setOnClickListener(v -> {
            Intent intentCampus = new Intent(MainActivity.this, CampusActivity.class);
            intentCampus.putExtra("USERNAME", username);
            startActivity(intentCampus);
        });

        taxesBtn.setOnClickListener(v -> {
            Intent intentTaxes = new Intent(MainActivity.this, TaxesActivity.class);
            intentTaxes.putExtra("USERNAME", username);
            startActivity(intentTaxes);
        });

        chatBtn.setOnClickListener(v -> {
            Intent intentChat = new Intent(MainActivity.this, ChatMainActivity.class);
            intentChat.putExtra("USERNAME", username);
            // Asigură-te că ai acces la variabila password
            intentChat.putExtra("PASSWORD", password); // Exemplu de placeholder pentru parolă
            startActivity(intentChat);
        });
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            // Permisiunea este deja acordată
            new LoadProfileImageTask().execute(username);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisiunea a fost acordată
                new LoadProfileImageTask().execute(username);
            } else {
                // Permisiunea nu a fost acordată
            }
        }
    }

    private void startActivityWithUsername(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    // Încărcare imagine de profil folosind Glide în MainActivity
    private class LoadProfileImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String profileImageUrl = null;
            Connection connection = null;

            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                Statement statement = connection.createStatement();
                String query = "SELECT ImagineProfil FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                ResultSet resultSet = statement.executeQuery(query);

                if (resultSet.next()) {
                    profileImageUrl = resultSet.getString("ImagineProfil");
                }

                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return profileImageUrl;
        }

        @Override
        protected void onPostExecute(String profileImageUrl) {
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                // Încarcă imaginea de profil în userImage folosind Glide
                Glide.with(MainActivity.this)
                        .load(profileImageUrl)
                        .into(userImage);
            } else {
                // Încarcă o imagine de încărcare sau altceva în caz de eroare
                Glide.with(MainActivity.this)
                        .load(R.drawable.loading)
                        .into(userImage);
            }
        }
    }

    private class GetNrMatricolTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... usernames) {
            String username = usernames[0];
            int nrMatricol = -1;
            Connection connection = null;

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                Statement statement = connection.createStatement();
                String query = "SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                ResultSet resultSet = statement.executeQuery(query);

                if (resultSet.next()) {
                    nrMatricol = resultSet.getInt("NrMatricol");
                }

                resultSet.close();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return nrMatricol;
        }

        @Override
        protected void onPostExecute(Integer result) {
            nrMatricol = result;
            Log.d("MainActivity", "Numărul matricolului: " + nrMatricol);
        }
    }

    private void showWeekSelectionDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Selectați săptămâna");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_week_selection, null);
        builder.setView(dialogView);

        Spinner spinnerWeek = dialogView.findViewById(R.id.weekSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.week_parity_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeek.setAdapter(adapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedWeek = spinnerWeek.getSelectedItem().toString();
                Intent intent;
                if (selectedWeek.equals("Săptămâna pară")) {
                    intent = new Intent(MainActivity.this, EvenWeekActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, OddWeekActivity.class);
                }
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Anulare", null);

        builder.create().show();
    }
}

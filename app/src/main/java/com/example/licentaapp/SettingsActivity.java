package com.example.licentaapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SettingsActivity extends AppCompatActivity {
    Connection con;
    String username;
    SwitchCompat switchMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String username = getIntent().getStringExtra("USERNAME");
        Log.d("SettingsActivity", "Numele de utilizator primit: " + username);

        switchMode = findViewById(R.id.switchMode);
        sharedPreferences = getSharedPreferences("night", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        boolean booleanValue = sharedPreferences.getBoolean("night_mode", true);
        switchMode.setChecked(booleanValue);

        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("night_mode", true);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("night_mode", false);
                }
                editor.apply();
            }
        });

        TextView changePasswordTextView = findViewById(R.id.changePassword);
        changePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText currentPasswordEditText = dialogView.findViewById(R.id.editTextCurrentPassword);
        EditText newPasswordEditText = dialogView.findViewById(R.id.editTextNewPassword);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.editTextConfirmPassword);

        builder.setPositiveButton("Salvează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPassword = currentPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(SettingsActivity.this, "Parolele noi nu coincid", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkCurrentPassword(currentPassword)) {
                    Toast.makeText(SettingsActivity.this, "Parola curentă incorectă", Toast.LENGTH_SHORT).show();
                    return;
                }

                updatePassword(newPassword);
            }
        });

        builder.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean checkCurrentPassword(String currentPassword) {
        String username = getIntent().getStringExtra("USERNAME");
        try {
            con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
            if (con == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String sql = "SELECT Parola FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    String storedPassword = rs.getString("Parola");
                    return currentPassword.equals(storedPassword);
                }

                rs.close();
                stmt.close();
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Tratează excepțiile SQL cum consideri necesar
        }

        return false;
    }

    private void updatePassword(String newPassword) {
        String username = getIntent().getStringExtra("USERNAME");
        try {
            con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
            if (con == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String query = "UPDATE Utilizator SET Parola = '" + newPassword + "' WHERE NumeUtilizator = '" + username + "'";
                Statement stmt = con.createStatement();
                int rowsAffected = stmt.executeUpdate(query);

                Log.d("SettingsActivity", "Noua parola: " + newPassword);
                Log.d("SettingsActivity", "Numele de utilizator: " + username);

                if (rowsAffected > 0) {
                    Toast.makeText(SettingsActivity.this, "Parola actualizată cu succes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Actualizarea parolei a eșuat", Toast.LENGTH_SHORT).show();
                }

                stmt.close();
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Tratează excepțiile SQL cum consideri necesar
        }
    }


    public Connection connectionClass(String user, String password, String db, String ip, String port) {
        StrictMode.ThreadPolicy a = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(a);
        Connection connection = null;
        String ConnectURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";user=" + user + ";" + "password=" + password + ";";
            connection = DriverManager.getConnection(ConnectURL);
        } catch (Exception e) {
            Log.e("Error is", e.getMessage());
        }
        return connection;
    }
}

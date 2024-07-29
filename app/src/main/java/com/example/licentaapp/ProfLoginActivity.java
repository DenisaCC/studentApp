package com.example.licentaapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfLoginActivity extends AppCompatActivity {

    private static ImageButton button_sbm;
    EditText profUsername, profPassword;
    Connection con;
    ToggleButton profToggleButtonShowPassword;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_login);

        // Initialize elements
        profUsername = findViewById(R.id.profUsername); // Asigură-te că ID-ul este corect
        profPassword = findViewById(R.id.profPassword); // Asigură-te că ID-ul este corect
        button_sbm = findViewById(R.id.profBackbtn); // Asigură-te că ID-ul este corect
        profToggleButtonShowPassword = findViewById(R.id.profToggleButtonShowPassword); // Asigură-te că ID-ul este corect

        OnClickButtonListener();

        profToggleButtonShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    profToggleButtonShowPassword.setBackgroundResource(R.drawable.baseline_visibility_off_24);
                    profPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isPasswordVisible = true;
                } else {
                    profToggleButtonShowPassword.setBackgroundResource(R.drawable.baseline_eye_24);
                    profPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                }
                profPassword.setSelection(profPassword.getText().length());
            }
        });

        Button loginbtn = findViewById(R.id.profLoginbtn); // Asigură-te că ID-ul este corect
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckLoginTask().execute("");
            }
        });
    }

    public class CheckLoginTask extends AsyncTask<String, String, String> {
        String z = null;
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String s) {
        }

        @Override
        protected String doInBackground(String... strings) {
            con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
            if (con == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfLoginActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show();
                    }
                });
                return "Fără conexiune la internet";
            } else {
                try {
                    String usernameStr = profUsername.getText().toString().trim();
                    String passwordStr = profPassword.getText().toString().trim();

                    if (!isValidPassword(passwordStr)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfLoginActivity.this, "Parola trebuie să conțină cel puțin 8 caractere, o cifră și o literă mare", Toast.LENGTH_LONG).show();
                            }
                        });
                        return "Parola nu respectă cerințele";
                    }

                    if (isPasswordVisible) {
                        profPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        profPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }

                    String sql = "SELECT * FROM UtilizatorProfesor WHERE NumeUtilizator = '" + usernameStr + "' AND Parola = '" + passwordStr + "'";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        final int professorId = rs.getInt("IDProfesor");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfLoginActivity.this, "Datele introduse sunt corecte!", Toast.LENGTH_LONG).show();
                            }
                        });
                        z = "Succes!";
                        Intent intent = new Intent(ProfLoginActivity.this, ProfMainActivity.class);
                        intent.putExtra("USERNAME", usernameStr);
                        intent.putExtra("PROFESSOR_ID", professorId);
                        intent.putExtra("PASSWORD", passwordStr);
                        startActivity(intent);
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfLoginActivity.this, "Verificați datele introduse!", Toast.LENGTH_LONG).show();
                            }
                        });
                        profUsername.setText("");
                        profPassword.setText("");
                    }
                } catch (Exception e) {
                    isSuccess = false;
                    Log.e("Eroare SQL : ", e.getMessage());
                }
            }
            return z;
        }

        private boolean isValidPassword(String password) {
            if (password.length() < 8) {
                return false;
            }
            String regex = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(password);
            return matcher.matches();
        }
    }

    public Connection connectionClass(String user, String password, String db, String ip, String port) {
        StrictMode.ThreadPolicy a = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(a);
        Connection connection = null;
        String ConnectURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";user=" + user + ";password=" + password + ";";
            connection = DriverManager.getConnection(ConnectURL);
        } catch (Exception e) {
            Log.e("Error is", e.getMessage());
        }
        return connection;
    }

    public void OnClickButtonListener() {
        button_sbm = findViewById(R.id.profBackbtn); // Asigură-te că ID-ul este corect
        button_sbm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfLoginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}

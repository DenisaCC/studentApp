package com.example.licentaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private static Button button_sbm;
    Connection con;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        OnClickButtonListener();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button loginbtn = findViewById(R.id.loginbtn);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheckLoginTask().execute("");
            }
        });

        TextView signupText = findViewById(R.id.signupText);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschideți o altă activitate pentru înregistrare
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
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
                        Toast.makeText(LoginActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show();
                    }
                });
                return "Fără conexiune la internet";
            } else {
                try {
                    String usernameStr = username.getText().toString().trim();
                    String passwordStr = password.getText().toString().trim();
                    // Verifică dacă parola respectă condițiile: minim 8 caractere, o cifră și o literă mare
                    if (!isValidPassword(passwordStr)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Parola trebuie să conțină cel puțin 8 caractere, o cifră și o literă mare", Toast.LENGTH_LONG).show();
                            }
                        });
                        return "Parola nu respectă cerințele";
                    }
                    String sql = "SELECT * FROM Utilizator WHERE NumeUtilizator = '" + usernameStr + "' AND Parola = '" + passwordStr + "'";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Datele introduse sunt corecte!", Toast.LENGTH_LONG).show();
                            }
                        });
                        z = "Succes!";
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USERNAME", usernameStr); // Trimite numele de utilizator către MainActivity
                        startActivity(intent);
                        finish(); // Termină activitatea curentă (LoginActivity)
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Verificați datele introduse!", Toast.LENGTH_LONG).show();
                            }
                        });
                        username.setText("");
                        password.setText("");
                    }
                } catch (Exception e) {
                    isSuccess = false;
                    Log.e("Eroare SQL : ", e.getMessage());
                }
            }
            return z;
        }

        // Metoda pentru a verifica dacă parola respectă condițiile
        private boolean isValidPassword(String password) {
            // Verifică dacă parola are cel puțin 8 caractere
            if (password.length() < 8) {
                return false;
            }
            // Verifică dacă parola conține cel puțin o cifră și o literă mare folosind expresii regulate
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
            ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";user=" + user + ";" + "password=" + password + ";";
            connection = DriverManager.getConnection(ConnectURL);
        } catch (Exception e) {
            Log.e("Error is", e.getMessage());
        }
        return connection;
    }

    public void OnClickButtonListener() {
        button_sbm = findViewById(R.id.backbtn);
        button_sbm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });
    }
}
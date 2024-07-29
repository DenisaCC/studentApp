package com.example.licentaapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.licentaapp.Connection.ConnectionClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private static ImageButton button_sbm;
    Connection con;
    EditText username, password;
    ToggleButton toggleButtonShowPassword;
    boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        OnClickButtonListener();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button loginbtn = findViewById(R.id.loginbtn);
        toggleButtonShowPassword = findViewById(R.id.toggleButtonShowPassword);

        // Inițializează Firebase Auth și Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = username.getText().toString().trim(); // Presupunând că email-ul este introdus în câmpul de username
                String passwordStr = password.getText().toString().trim();

                if (checkIfAdmin(emailStr)) {
                    // Utilizatorul este admin, autentifică-l cu Firebase Authentication
                    firebaseSignIn(emailStr, passwordStr);
                } else {
                    // Utilizatorul nu este admin, continuă cu verificarea și autentificarea ca și înainte, probabil utilizând baza de date SSMS
                    new CheckLoginTask().execute("");
                }
            }
        });

        TextView signupText = findViewById(R.id.signupText);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView profLoginText = findViewById(R.id.profLoginText);
        profLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deschide ProfLoginActivity când TextView-ul este apăsat
                Intent intent = new Intent(LoginActivity.this, ProfLoginActivity.class);
                startActivity(intent);
            }
        });

        toggleButtonShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleButtonShowPassword.setBackgroundResource(R.drawable.baseline_visibility_off_24);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isPasswordVisible = true;
                } else {
                    toggleButtonShowPassword.setBackgroundResource(R.drawable.baseline_eye_24);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isPasswordVisible = false;
                }
                password.setSelection(password.getText().length());
            }
        });
    }

    public class CheckLoginTask extends AsyncTask<String, String, String> {
        String z = null;
        Boolean isSuccess = false;
        String passwordStr = "";
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String s) {
        }

        public String getPassword() {
            return passwordStr;
        }
        @Override
        protected String doInBackground(String... strings) {
            con = ConnectionClass.connect(); // Utilizează metoda connect() din ConnectionClass
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
                    passwordStr = password.getText().toString().trim();
                    if (!isValidPassword(passwordStr)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Parola trebuie să conțină cel puțin 8 caractere, o cifră și o literă mare", Toast.LENGTH_LONG).show();
                            }
                        });
                        return "Parola nu respectă cerințele";
                    }

                    // Verifică dacă toggleButton este activat sau dezactivat și setează parola corespunzător
                    if (isPasswordVisible) {
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
                        intent.putExtra("USERNAME", usernameStr);
                        intent.putExtra("PASSWORD", passwordStr);
                        startActivity(intent);
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

    private boolean checkIfAdmin(String email) {
        return email.equals("studentappuniversitate@gmail.com");
    }

    private void firebaseSignIn(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Autentificarea a fost reușită
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            String username = firebaseUser.getDisplayName();
                            saveAdminUserToDatabase(firebaseUser);

                            Intent intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                            intent.putExtra("name", username);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Autentificarea a eșuat
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            // Utilizatorul nu există
                            Toast.makeText(LoginActivity.this, "Utilizatorul nu există", Toast.LENGTH_SHORT).show();
                        } else {
                            // Parola introdusă este incorectă sau a avut loc o altă eroare
                            Toast.makeText(LoginActivity.this, "Nu aveti acces la contul de administrator", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveAdminUserToDatabase(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String displayName = firebaseUser.getDisplayName();

        // Structura obiectului User
        User adminUser = new User(displayName, email, "admin");

        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Administrators");
        adminRef.child(userId).setValue(adminUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "Admin user added to Realtime Database.");
                        } else {
                            Log.e("Firebase", "Failed to add admin user.", task.getException());
                        }
                    }
                });
    }

    // Structura obiectului User
    public static class User {
        public String name;
        public String email;
        public String role;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }
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

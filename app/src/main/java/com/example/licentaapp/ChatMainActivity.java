package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.ProfessorAdapter;
import com.example.licentaapp.Utils.ProfessorModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatMainActivity extends AppCompatActivity implements ProfessorAdapter.SignInListener {

    private RecyclerView recyclerView;
    private ProfessorAdapter professorAdapter;
    private String username;
    private String senderId;
    private String password;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        professorAdapter = new ProfessorAdapter(this, this);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(professorAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("USERNAME");
            senderId = intent.getStringExtra("SENDER_ID");
            password = intent.getStringExtra("PASSWORD");

            Log.d("ChatMainActivity", "Received intent with USERNAME: " + username + ", SENDER_ID: " + senderId + ", PASSWORD: " + password);

            int nrMatricol = getNrMatricolForUsername(username);
            if (nrMatricol != -1) {
                getProfessorsByFaculty(nrMatricol);
            } else {
                Toast.makeText(ChatMainActivity.this, "Utilizatorul nu a fost găsit în SSMS19", Toast.LENGTH_SHORT).show();
            }
        }

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatMainActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (username != null) {
            int nrMatricol = getNrMatricolForUsername(username);
            if (nrMatricol != -1) {
                professorAdapter.clear(); // Curățăm lista înainte de a adăuga date noi
                getProfessorsByFaculty(nrMatricol);
            }
        }
    }


    private int getNrMatricolForUsername(String username) {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                Log.d("ChatMainActivity", "Executing query: " + query); // Adăugăm log pentru query
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int nrMatricol = rs.getInt("NrMatricol");
                    Log.d("ChatMainActivity", "Found NrMatricol: " + nrMatricol); // Adăugăm log pentru numărul matricol găsit
                    return nrMatricol;
                } else {
                    Log.d("ChatMainActivity", "No NrMatricol found for username: " + username); // Adăugăm log pentru cazul în care nu se găsește numărul matricol
                    return -1;
                }
            } catch (SQLException e) {
                Log.e("ChatMainActivity", "SQLException in getNrMatricolForUsername", e); // Adăugăm log pentru excepții SQL
                return -1;
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
        return -1;
    }

    private void getProfessorsByFaculty(int nrMatricol) {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT DISTINCT UP.NumeUtilizator, UP.IDProfesor FROM UtilizatorProfesor UP " +
                        "INNER JOIN Profesor_Facultate PF ON UP.IDProfesor = PF.IDProfesor " +
                        "INNER JOIN Student S ON PF.IDFacultate = S.IDFacultate " +
                        "WHERE S.NrMatricol = " + nrMatricol;
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String professorId = rs.getString("IDProfesor");
                    String professorUsername = rs.getString("NumeUtilizator");
                    ProfessorModel professorModel = new ProfessorModel(professorId, professorUsername);
                    Log.d("ChatMainActivity", "Professor: " + professorModel.getName());
                    professorAdapter.add(professorModel);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
    }

    public void firebaseSignIn(String email, String password) {
        Log.d("ChatMainActivity", "Trying to sign in with email: " + email);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Autentificare cu succes
                        Log.d("ChatMainActivity", "Autentificare Firebase reușită");
                        Toast.makeText(ChatMainActivity.this, "Autentificare cu succes!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChatMainActivity.this, ChatActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Autentificare eșuată
                        Log.e("ChatMainActivity", "Autentificare Firebase eșuată", e);
                        Toast.makeText(ChatMainActivity.this, "Autentificare eșuată. Verificați email-ul și parola.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onSignIn(String email, String password) {
        Log.d("ChatMainActivity", "Sign in button clicked with email: " + email + " and password: " + password);

        // Verificați dacă parola introdusă în dialog este aceeași cu cea din logarea cu SSMS
        if (password.equals(this.password)) {
            // Parolele sunt identice, apelați metoda firebaseSignIn
            firebaseSignIn(email, password);
        } else {
            // Parolele nu sunt identice, afișați un mesaj către utilizator
            Toast.makeText(ChatMainActivity.this, "Datele introduse sunt greșite.", Toast.LENGTH_SHORT).show();
        }
    }

}

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
import com.example.licentaapp.Utils.StudentAdapter;
import com.example.licentaapp.Utils.StudentModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfChatMainActivity extends AppCompatActivity implements StudentAdapter.OnItemClickListener {

    private static final String TAG = "ProfChatMainActivity";

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private String senderId;
    private String professorUsername, professorPassword; // Obtained from MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_chat_main);

        studentAdapter = new StudentAdapter(this, new StudentAdapter.SignInListener() {
            @Override
            public void onSignIn(String email, String password) {
                firebaseSignIn(email, password);
            }
        });
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(studentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentAdapter.setOnItemClickListener(this);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfChatMainActivity.this, ProfMainActivity.class);
            startActivity(intent);
            finish();
        });

        Intent intent = getIntent();
        if (intent != null) {
            professorUsername = intent.getStringExtra("USERNAME");
            senderId = intent.getStringExtra("SENDER_ID");
            professorPassword = intent.getStringExtra("PASSWORD");
            Log.d(TAG, "Username: " + professorUsername);
            Log.d(TAG, "Sender ID: " + senderId);
            Log.d(TAG, "Password: " + professorPassword);
            getIDProfesor(professorUsername);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (professorUsername != null) {
            studentAdapter.clear();
            getIDProfesor(professorUsername);
        }
    }

    private void getIDProfesor(String username) {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT IDProfesor FROM UtilizatorProfesor WHERE NumeUtilizator = '" + username + "'";
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int idProfesor = rs.getInt("IDProfesor");
                    getStudentsByFaculty(idProfesor);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(TAG, "Error retrieving professor ID: " + e.getMessage());
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
    }

    private void getStudentsByFaculty(int idProfesor) {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT DISTINCT S.Nume, S.Prenume, S.AnStudiu, SP.Nume AS Specializare " +
                        "FROM Student S " +
                        "INNER JOIN Profesor_Facultate PF ON S.IDFacultate = PF.IDFacultate " +
                        "INNER JOIN Specializare SP ON S.IDSpecializare = SP.IDSpecializare " +
                        "WHERE PF.IDProfesor = " + idProfesor;

                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String nume = rs.getString("Nume");
                    String prenume = rs.getString("Prenume");
                    int anStudiu = rs.getInt("AnStudiu");
                    String specializare = rs.getString("Specializare");

                    StudentModel studentModel = new StudentModel(nume + " " + prenume, anStudiu, specializare);
                    studentAdapter.add(studentModel);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(TAG, "Error retrieving students by faculty: " + e.getMessage());
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        StudentModel selectedStudent = studentAdapter.getItem(position);
        if (selectedStudent != null) {
            Intent intent = new Intent(ProfChatMainActivity.this, ProfChatActivity.class);
            intent.putExtra("STUDENT_USERNAME", selectedStudent.getName()); // Pass student username
            intent.putExtra("USERNAME", professorUsername);
            intent.putExtra("PASSWORD", professorPassword);
            intent.putExtra("STUDENT_ID", selectedStudent.getId()); // Pass student ID
            startActivity(intent);
        }
    }

    public void firebaseSignIn(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                        Toast.makeText(ProfChatMainActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ProfChatMainActivity.this, ProfChatActivity.class);
                        intent.putExtra("USERNAME", professorUsername);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfChatMainActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

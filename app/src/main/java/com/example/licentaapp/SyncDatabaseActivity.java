package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SyncDatabaseActivity extends AppCompatActivity {

    boolean studentDataSynced = false;
    boolean professorDataSynced = false;
    int syncCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_database);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SyncDatabaseActivity.this, MainAdminActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        Button syncButton = findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adaugă aici logica pentru sincronizarea bazelor de date din SSMS19 în Firebase
                syncDataFromSSMS19ToFirebase();
            }
        });
    }

    private void syncDataFromSSMS19ToFirebase() {
        syncCount = 0; // Resetăm semaforul
        syncStudentDataFromSSMS19ToFirebase();
        syncProfessorDataFromSSMS19ToFirebase();
    }

    private void syncStudentDataFromSSMS19ToFirebase() {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Utilizator");
                while (rs.next()) {
                    // Extrage datele pentru student din ResultSet
                    int studentId = rs.getInt("IDUtilizator");
                    String studentName = rs.getString("NumeUtilizator");
                    String studentEmail = rs.getString("Email");
                    String studentPass = rs.getString("Parola");
                    int nrMatricol = rs.getInt("NrMatricol");
                    String profileImage = rs.getString("ImagineProfil");

                    // Verifică dacă datele există deja în Firebase
                    DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("students");
                    studentsRef.orderByChild("email").equalTo(studentEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                // Dacă datele nu există, sincronizează-le în Firebase
                                String studentFirebaseId = studentsRef.push().getKey();
                                studentsRef.child(studentFirebaseId).child("studentID").setValue(studentId);
                                studentsRef.child(studentFirebaseId).child("name").setValue(studentName);
                                studentsRef.child(studentFirebaseId).child("email").setValue(studentEmail);
                                studentsRef.child(studentFirebaseId).child("password").setValue(studentPass);
                                studentsRef.child(studentFirebaseId).child("nrMatricol").setValue(nrMatricol);
                                studentsRef.child(studentFirebaseId).child("profileImage").setValue(profileImage);
                                studentsRef.child(studentFirebaseId).child("role").setValue("student");

                                // Crearea unui cont Firebase Auth
                                createFirebaseAccount(studentEmail, studentPass, studentName);
                                studentDataSynced = true;
                            }
                            syncCount++; // Incrementăm semaforul
                            checkSyncStatus(); // Verificăm dacă am terminat sincronizarea
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle potential errors here
                        }
                    });

                    // Continuă sincronizarea celorlalte date în funcție de necesități
                }
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void syncProfessorDataFromSSMS19ToFirebase() {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM UtilizatorProfesor");
                while (rs.next()) {
                    // Extrage datele pentru profesor din ResultSet
                    int professorId = rs.getInt("IDUtilizator");
                    String professorName = rs.getString("NumeUtilizator");
                    String professorEmail = rs.getString("Email");
                    String professorPass = rs.getString("Parola");
                    int idProfesor = rs.getInt("IDProfesor");
                    String profileImage = rs.getString("ImagineProfil");

                    // Verifică dacă datele există deja în Firebase
                    DatabaseReference professorsRef = FirebaseDatabase.getInstance().getReference("professors");
                    professorsRef.orderByChild("email").equalTo(professorEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                // Dacă datele nu există, sincronizează-le în Firebase
                                String professorFirebaseId = professorsRef.push().getKey();
                                professorsRef.child(professorFirebaseId).child("professorID").setValue(professorId);
                                professorsRef.child(professorFirebaseId).child("name").setValue(professorName);
                                professorsRef.child(professorFirebaseId).child("email").setValue(professorEmail);
                                professorsRef.child(professorFirebaseId).child("password").setValue(professorPass);
                                professorsRef.child(professorFirebaseId).child("idProfesor").setValue(idProfesor);
                                professorsRef.child(professorFirebaseId).child("profileImage").setValue(profileImage);
                                professorsRef.child(professorFirebaseId).child("role").setValue("professor");

                                // Crearea unui cont Firebase Auth
                                createFirebaseAccount(professorEmail, professorPass, professorName);
                                professorDataSynced = true;
                            }
                            syncCount++; // Incrementăm semaforul
                            checkSyncStatus(); // Verificăm dacă am terminat sincronizarea
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle potential errors here
                        }
                    });

                    // Continuă sincronizarea celorlalte date în funcție de necesități
                }
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createFirebaseAccount(String email, String password, String displayName) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set display name
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            Toast.makeText(SyncDatabaseActivity.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SyncDatabaseActivity.this, "Failed to create user in Firebase Auth", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkSyncStatus() {
        if (syncCount >= 2) { // Dacă am terminat sincronizarea pentru ambele tabele
            if (studentDataSynced || professorDataSynced) {
                Toast.makeText(this, "Datele au fost sincronizate cu succes!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nu există utilizatori noi în baza de date.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

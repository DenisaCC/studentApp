package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.text.InputFilter;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsertGradeActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewStudent;
    private AutoCompleteTextView autoCompleteTextViewDiscipline;
    private TextInputEditText editTextNota;
    private TextInputEditText editTextDataNota;
    private Button buttonIncarcaNota;
    private String username;
    private int selectedDisciplineId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_grade);

        autoCompleteTextViewStudent = findViewById(R.id.autoCompleteTextViewStudent);
        autoCompleteTextViewDiscipline = findViewById(R.id.autoCompleteTextViewDiscipline);
        editTextNota = findViewById(R.id.editTextNota);
        editTextDataNota = findViewById(R.id.editTextDataNota);
        buttonIncarcaNota = findViewById(R.id.addBtn);
        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InsertGradeActivity.this, ProfMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("USERNAME");
            Log.d("Insert/grade", username);
        }

        // Setează data curentă în câmpul de dată
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editTextDataNota.setText(currentDate);

        // Populează lista de discipline
        populateDisciplines();
        populateStudents();

        // Adaugă filtrul de intrare pentru câmpul de notă
        editTextNota.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2), new InputFilter.AllCaps()});
        editTextNota.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextNota.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)}); // Limitează lungimea maximă a textului la 1


        buttonIncarcaNota.setOnClickListener(v -> {
            // Colectează datele și gestionează logica de încărcare a notei
            String student = autoCompleteTextViewStudent.getText().toString();
            String disciplina = autoCompleteTextViewDiscipline.getText().toString();
            String notaStr = editTextNota.getText().toString();
            String dataNota = editTextDataNota.getText().toString();

            // Verifică dacă toate câmpurile sunt completate
            if (!student.isEmpty() && !disciplina.isEmpty() && !notaStr.isEmpty() && !dataNota.isEmpty()) {
                try {
                    float nota = Float.parseFloat(notaStr);

                    // Inserează nota în baza de date
                    insertGrade(student, disciplina, nota, dataNota);

                    // Afiseaza mesaj de succes
                    Toast.makeText(InsertGradeActivity.this, "Nota a fost încărcată", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    // Afiseaza mesaj de eroare daca nota introdusa nu este un numar valid
                    Toast.makeText(InsertGradeActivity.this, "Nota trebuie să fie un număr valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Afiseaza mesaj de eroare daca nu sunt completate toate campurile
                Toast.makeText(InsertGradeActivity.this, "Completează toate câmpurile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertGrade(String student, String disciplina, float nota, String dataNota) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                // Obțineți ID-urile pentru student, profesor și disciplină folosind funcțiile existente
                int studentID = getStudentID(student, conn);
                int disciplineID = selectedDisciplineId;
                int professorID = getProfessorId(username);

                if (studentID != -1 && disciplineID != -1 && professorID != -1) {
                    // Inserează nota în baza de date folosind PreparedStatement
                    String query = "INSERT INTO Nota (NrMatricol, IDProfesor, IDDisciplina, Nota, DataNota) VALUES (?, ?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, studentID);
                    pstmt.setInt(2, professorID);
                    pstmt.setInt(3, disciplineID);
                    pstmt.setFloat(4, nota);
                    pstmt.setString(5, dataNota);
                    pstmt.executeUpdate();
                } else {
                    Log.e("Insert Grade Error", "Studentul, profesorul sau disciplina nu există în baza de date");
                }
            } else {
                Toast.makeText(this, "Conexiunea la baza de date nu poate fi stabilită", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Log.e("Insert Grade Error", "Error inserting grade", e);
        } finally {
            ConnectionClass.closeConnection(conn);
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Insert Grade Error", e.getMessage());
                }
            }
        }
    }

    private void populateDisciplines() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                String query = "SELECT D.Nume FROM Disciplina D WHERE D.IDProfesor = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, getProfessorId(username));
                rs = pstmt.executeQuery();

                List<String> disciplineNames = new ArrayList<>();
                while (rs.next()) {
                    String disciplineName = rs.getString("Nume");
                    // Verificăm dacă disciplina nu este deja în listă pentru a evita duplicarea
                    if (!disciplineNames.contains(disciplineName)) {
                        disciplineNames.add(disciplineName);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, disciplineNames);
                autoCompleteTextViewDiscipline.setAdapter(adapter);

                // Adăugăm un Listener pentru selectarea unei discipline
                autoCompleteTextViewDiscipline.setOnItemClickListener((parent, view, position, id) -> {
                    // Obținem disciplina selectată din lista derulantă
                    String selectedDiscipline = (String) parent.getItemAtPosition(position);
                    // Obținem ID-ul disciplinei selectate și îl salvăm în variabila selectedDisciplineId
                    selectedDisciplineId = getDisciplineId(selectedDiscipline);
                });
            } else {
                Toast.makeText(this, "Conexiunea la baza de date nu poate fi stabilită", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Log.e("Error", e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            ConnectionClass.closeConnection(conn);
        }
    }

    private int getProfessorId(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int professorId = -1;

        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                String query = "SELECT IDProfesor FROM UtilizatorProfesor WHERE NumeUtilizator = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    professorId = rs.getInt("IDProfesor");
                }
            }
        } catch (SQLException e) {
            Log.e("SQL Exception", e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            ConnectionClass.closeConnection(conn);
        }
        return professorId;
    }

    private int getDisciplineId(String disciplineName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int disciplineId = -1;

        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                String query = "SELECT IDDisciplina FROM Disciplina WHERE Nume = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, disciplineName);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    disciplineId = rs.getInt("IDDisciplina");
                }
            }
        } catch (SQLException e) {
            Log.e("SQL Exception", e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            ConnectionClass.closeConnection(conn);
        }
        return disciplineId;
    }

    private void populateStudents() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                String query = "SELECT DISTINCT CONCAT(S.Nume, ' ', S.Prenume) AS NumeComplet " +
                        "FROM Student S " +
                        "INNER JOIN Disciplina_Specializare DS ON S.IDSpecializare = DS.IDSpecializare " +
                        "INNER JOIN Disciplina D ON DS.IDDisciplina = D.IDDisciplina " +
                        "INNER JOIN UtilizatorProfesor UP ON D.IDProfesor = UP.IDProfesor " +
                        "WHERE UP.NumeUtilizator = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                rs = pstmt.executeQuery();
                List<String> studentList = new ArrayList<>();
                while (rs.next()) {
                    String numeComplet = rs.getString("NumeComplet");
                    studentList.add(numeComplet);
                }

                // Populăm lista derulantă pentru student cu numele studenților
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                autoCompleteTextViewStudent.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e("Populate Students Error", "Error populating students", e);
            Toast.makeText(this, "A apărut o eroare la popularea listei cu studenți.", Toast.LENGTH_SHORT).show();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            ConnectionClass.closeConnection(conn);
        }
    }

    private int getStudentID(String studentFullName, Connection conn) {
        int studentID = -1;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Split numele complet în nume și prenume
            String[] parts = studentFullName.split("\\s+");
            String nume = parts[0];
            String prenume = parts[1];

            // Interogare pentru a găsi NrMatricol-ul studentului pe baza numelui și prenumelui
            String query = "SELECT NrMatricol FROM Student WHERE Nume = ? AND Prenume = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nume);
            pstmt.setString(2, prenume);
            rs = pstmt.executeQuery();

            // Dacă găsim un rezultat, extragem NrMatricol-ul
            if (rs.next()) {
                studentID = rs.getInt("NrMatricol");
            }
        } catch (SQLException e) {
            Log.e("Get Student ID Error", e.getMessage());
        } finally {
            // Închidem resursele
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e("Get Student ID Error", e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    Log.e("Get Student ID Error", e.getMessage());
                }
            }
        }
        return studentID;
    }

}

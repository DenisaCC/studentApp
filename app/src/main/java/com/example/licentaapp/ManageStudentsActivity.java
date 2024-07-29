package com.example.licentaapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.UserStudentAdapter;
import com.example.licentaapp.Utils.UserStudentModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity implements UserStudentAdapter.OnDeleteClickListener, UserStudentAdapter.OnEditClickListener {

    private RecyclerView recyclerView;
    private UserStudentAdapter adapter;
    private List<UserStudentModel> students;
    private static final String TAG = "ManageStudentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserStudentAdapter();
        adapter.setOnDeleteClickListener(this);
        adapter.setOnEditClickListener(this);
        recyclerView.setAdapter(adapter);

        students = getStudentsFromDatabase();
        adapter.setStudents(students);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());
    }

    private List<UserStudentModel> getStudentsFromDatabase() {
        List<UserStudentModel> students = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                Log.d(TAG, "Conexiune cu succes la baza de date.");

                String query = "SELECT NumeUtilizator, Email, Parola, NrMatricol FROM Utilizator";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String username = rs.getString("NumeUtilizator");
                    String email = rs.getString("Email");
                    String password = rs.getString("Parola");
                    int nrMatricol = rs.getInt("NrMatricol");

                    UserStudentModel student = new UserStudentModel(nrMatricol, username, email, password);
                    students.add(student);
                }
            } else {
                Log.e(TAG, "Conexiunea la baza de date a eșuat.");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Excepție SQL: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                Log.e(TAG, "Eroare la închiderea resurselor: " + e.getMessage());
            }
        }

        return students;
    }

    @Override
    public void onDeleteClick(int nrMatricol, int position) {
        deleteStudent(nrMatricol, position);
    }

    @Override
    public void onEditClick(UserStudentModel student, int position) {
        showEditStudentDialog(student, position);
    }

    private void showEditStudentDialog(UserStudentModel student, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editare Detalii Student");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_student, null);
        builder.setView(dialogView);

        EditText textNrMatricol = dialogView.findViewById(R.id.editNrMatricol);
        EditText editTextUsername = dialogView.findViewById(R.id.editName);
        EditText editTextEmail = dialogView.findViewById(R.id.editEmail);
        EditText editTextPassword = dialogView.findViewById(R.id.editPassword);

        textNrMatricol.setText(String.valueOf(student.getNrMatricol()));
        editTextUsername.setText(student.getUsername());
        editTextUsername.setText(student.getUsername());
        editTextEmail.setText(student.getEmail());
        editTextPassword.setText(student.getPassword());

        builder.setPositiveButton("Salvează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUsername = editTextUsername.getText().toString();
                String newEmail = editTextEmail.getText().toString();
                String newPassword = editTextPassword.getText().toString();

                if (!newUsername.isEmpty() && !newEmail.isEmpty() && !newPassword.isEmpty()) {
                    updateStudentDetails(student, newUsername, newEmail, newPassword, position);
                } else {
                    Toast.makeText(ManageStudentsActivity.this, "Toate câmpurile sunt obligatorii", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Anulează", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateStudentDetails(UserStudentModel student, String newUsername, String newEmail, String newPassword, int position) {
        Connection conn = null;
        PreparedStatement stmtUtilizator = null;
        PreparedStatement stmtStudent = null;

        try {
            conn = ConnectionClass.connect();

            if (conn != null) {
                Log.d(TAG, "Connected to the database");

                // Actualizează tabela Utilizator
                String sqlUtilizator = "UPDATE Utilizator SET NumeUtilizator = ?, Email = ?, Parola = ? WHERE NrMatricol = ?";
                stmtUtilizator = conn.prepareStatement(sqlUtilizator);
                stmtUtilizator.setString(1, newUsername);
                stmtUtilizator.setString(2, newEmail);
                stmtUtilizator.setString(3, newPassword);
                stmtUtilizator.setInt(4, student.getNrMatricol());

                int affectedRowsUtilizator = stmtUtilizator.executeUpdate();

                // Actualizează tabela Student
                String sqlStudent = "UPDATE Student SET Email = ? WHERE NrMatricol = ?";
                stmtStudent = conn.prepareStatement(sqlStudent);
                stmtStudent.setString(1, newEmail);
                stmtStudent.setInt(2, student.getNrMatricol());

                int affectedRowsStudent = stmtStudent.executeUpdate();

                if (affectedRowsUtilizator > 0 && affectedRowsStudent > 0) {
                    Toast.makeText(this, "Detaliile studentului au fost actualizate cu succes", Toast.LENGTH_SHORT).show();
                    student.setUsername(newUsername);
                    student.setEmail(newEmail);
                    student.setPassword(newPassword);
                    students.set(position, student);
                    adapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(this, "Actualizarea detaliilor a eșuat", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to connect to the database");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        } finally {
            try {
                if (stmtUtilizator != null) {
                    stmtUtilizator.close();
                }
                if (stmtStudent != null) {
                    stmtStudent.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, "SQL Exception during resource closing: " + e.getMessage());
            }
        }
    }

    private void deleteStudent(int nrMatricol, int position) {
        Log.d(TAG, "Deleting student with ID: " + nrMatricol);
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        boolean isUserDeleted = false;
        boolean isStudentDeleted = false;

        try {
            // Connect to the database
            conn = ConnectionClass.connect();

            if (conn != null) {
                Log.d(TAG, "Connected to the database");

                // Start a transaction
                conn.setAutoCommit(false);

                // Delete from Utilizator table
                String sql1 = "DELETE FROM Utilizator WHERE NrMatricol = ?";
                stmt1 = conn.prepareStatement(sql1);
                stmt1.setInt(1, nrMatricol);
                int affectedRows1 = stmt1.executeUpdate();
                isUserDeleted = (affectedRows1 > 0);

                if (isUserDeleted) {
                    // Delete from Student table
                    String sql2 = "DELETE FROM Student WHERE NrMatricol = ?";
                    stmt2 = conn.prepareStatement(sql2);
                    stmt2.setInt(1, nrMatricol);
                    int affectedRows2 = stmt2.executeUpdate();
                    isStudentDeleted = (affectedRows2 > 0);

                    if (isStudentDeleted) {
                        // Commit the transaction if both deletions are successful
                        conn.commit();
                        // Remove the student from the list and notify the adapter
                        students.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Studentul a fost șters cu succes", Toast.LENGTH_SHORT).show();
                    } else {
                        // Rollback the transaction if the student deletion fails
                        conn.rollback();
                        Toast.makeText(this, "Ștergerea studentului a eșuat", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Ștergerea utilizatorului a eșuat", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to connect to the database");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback the transaction on exception
                }
            } catch (SQLException rollbackException) {
                Log.e(TAG, "Rollback Exception: " + rollbackException.getMessage());
            }
        } finally {
            // Close the resources
            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (stmt2 != null) {
                    stmt2.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, "SQL Exception during resource closing: " + e.getMessage());
            }
        }
    }
}


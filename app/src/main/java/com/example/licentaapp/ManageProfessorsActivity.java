package com.example.licentaapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.UserProfessorAdapter;
import com.example.licentaapp.Utils.UserProfessorModel;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageProfessorsActivity extends AppCompatActivity implements UserProfessorAdapter.OnDeleteClickListener, UserProfessorAdapter.OnEditClickListener {

    private RecyclerView recyclerView;
    private UserProfessorAdapter adapter;
    private List<UserProfessorModel> professors;
    private ImageView imageViewProfile;
    private static final String TAG = "ManageProfessorsActivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_professors);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewProfessors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProfessorAdapter();
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());

        // Extract data from SQL Server database
        professors = getProfessorsFromDatabase(); // Update the member variable
        adapter.setProfessors(professors);

        // Setup the adapter's click listeners
        setupRecyclerView();
    }

    private List<UserProfessorModel> getProfessorsFromDatabase() {
        List<UserProfessorModel> professors = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            conn = ConnectionClass.connect();

            if (conn != null) {
                Log.d(TAG, "Connected to the database");

                // Create a statement object
                stmt = conn.prepareStatement("SELECT NumeUtilizator, Email, IDProfesor, Parola FROM UtilizatorProfesor");

                // Execute the query
                rs = stmt.executeQuery();

                // Process the result set
                while (rs.next()) {
                    String numeUtilizator = rs.getString("NumeUtilizator");
                    String email = rs.getString("Email");
                    int idProfessor = rs.getInt("IDProfesor");
                    String password = rs.getString("Parola");

                    // Create a UserProfessorModel object and add it to the list
                    UserProfessorModel professor = new UserProfessorModel(numeUtilizator, email, password, idProfessor);
                    professors.add(professor);
                }
            } else {
                Log.e(TAG, "Failed to connect to the database");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        } finally {
            // Close the resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, "SQL Exception during resource closing: " + e.getMessage());
            }
        }

        return professors;
    }

    private void setupRecyclerView() {
        adapter.setOnDeleteClickListener(this);
        adapter.setOnEditClickListener(this);
    }

    @Override
    public void onDeleteClick(int position) {
        // Get the professor's ID based on the position in the list
        int professorId = professors.get(position).getIdProfessor();
        Log.d(TAG, "Attempting to delete professor with ID: " + professorId);

        // Delete professor from database
        String professorEmail = professors.get(position).getEmail();
        deleteProfessor(professorId, position);
    }

    @Override
    public void onEditClick(int position) {
        // Get the professor's details based on the position in the list
        UserProfessorModel professor = professors.get(position);
        openEditDialog(professor, position);
    }

    private void deleteProfessor(int professorId, int position) {
        Log.d(TAG, "Deleting professor with ID: " + professorId);
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;

        try {
            // Connect to the database
            conn = ConnectionClass.connect();

            if (conn != null) {
                Log.d(TAG, "Connected to the database");

                // Start a transaction
                conn.setAutoCommit(false);

                // Delete from Disciplina table
                String sql1 = "DELETE FROM Disciplina WHERE IDProfesor = ?";
                stmt1 = conn.prepareStatement(sql1);
                stmt1.setInt(1, professorId);
                int affectedRows1 = stmt1.executeUpdate();
                Log.d(TAG, "Deleted from Disciplina: " + affectedRows1 + " rows affected");

                // Delete from Profesor_Facultate table
                String sql2 = "DELETE FROM Profesor_Facultate WHERE IDProfesor = ?";
                stmt2 = conn.prepareStatement(sql2);
                stmt2.setInt(1, professorId);
                int affectedRows2 = stmt2.executeUpdate();
                Log.d(TAG, "Deleted from Profesor_Facultate: " + affectedRows2 + " rows affected");

                // Delete from UtilizatorProfesor table
                String sql3 = "DELETE FROM UtilizatorProfesor WHERE IDProfesor = ?";
                stmt3 = conn.prepareStatement(sql3);
                stmt3.setInt(1, professorId);
                int affectedRows3 = stmt3.executeUpdate();
                Log.d(TAG, "Deleted from UtilizatorProfesor: " + affectedRows3 + " rows affected");

                // Delete from Profesor table
                String sql4 = "DELETE FROM Profesor WHERE IDProfesor = ?";
                stmt4 = conn.prepareStatement(sql4);
                stmt4.setInt(1, professorId);
                int affectedRows4 = stmt4.executeUpdate();
                Log.d(TAG, "Deleted from Profesor: " + affectedRows4 + " rows affected");

                // Commit the transaction regardless of whether the deletions affect any rows
                conn.commit();
                Log.d(TAG, "Transaction committed successfully");
                // Remove the professor from the list and notify the adapter
                professors.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(this, "Profesorul a fost șters cu succes", Toast.LENGTH_SHORT).show();

                // Additional logging to indicate if any deletions did not affect any rows
                if (affectedRows1 == 0) {
                    Log.w(TAG, "No rows affected in Disciplina table for IDProfesor: " + professorId);
                }
                if (affectedRows2 == 0) {
                    Log.w(TAG, "No rows affected in Profesor_Facultate table for IDProfesor: " + professorId);
                }
                if (affectedRows3 == 0) {
                    Log.w(TAG, "No rows affected in UtilizatorProfesor table for IDProfesor: " + professorId);
                }
                if (affectedRows4 == 0) {
                    Log.w(TAG, "No rows affected in Profesor table for IDProfesor: " + professorId);
                }

            } else {
                Log.e(TAG, "Failed to connect to the database");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback the transaction on exception
                    Log.e(TAG, "Transaction rolled back due to SQL exception");
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
                if (stmt3 != null) {
                    stmt3.close();
                }
                if (stmt4 != null) {
                    stmt4.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, "SQL Exception during resource closing: " + e.getMessage());
            }
        }
    }

    private void openEditDialog(UserProfessorModel professor, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_professor, null);
        builder.setView(view);

        // Initialize the edit fields
        EditText editName = view.findViewById(R.id.editName);
        EditText editEmail = view.findViewById(R.id.editEmail);
        EditText editIdProfessor = view.findViewById(R.id.editIdProfessor);
        EditText editPassword = view.findViewById(R.id.editPassword);

        // Set initial values
        editName.setText(professor.getNumeUtilizator());
        editEmail.setText(professor.getEmail());
        editIdProfessor.setText(String.valueOf(professor.getIdProfessor()));

        // Set initial password value if you have it stored or fetched from database
        editPassword.setText(professor.getPassword());

        builder.setPositiveButton("Salvare", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get updated values
                String newName = editName.getText().toString();
                String newEmail = editEmail.getText().toString();
                String newPassword = editPassword.getText().toString();

                // Update the professor details in the database
                updateProfessorInDatabase(professor.getIdProfessor(), newName, newEmail, newPassword);

                // Update the professor details in the list and notify the adapter
                professor.setNumeUtilizator(newName);
                professor.setEmail(newEmail);
                professor.setPassword(newPassword); // Update the password in the model
                adapter.notifyItemChanged(position);
            }
        });

        builder.setNegativeButton("Anulare", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updateProfessorInDatabase(int professorId, String newName, String newEmail, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connect to the database
            conn = ConnectionClass.connect();

            if (conn != null) {
                Log.d(TAG, "Connected to the database");

                // Update the professor details
                String sql = "UPDATE UtilizatorProfesor SET NumeUtilizator = ?, Email = ?, Parola = ? WHERE IDProfesor = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newEmail);
                stmt.setString(3, newPassword); // Update the password
                stmt.setInt(4, professorId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    Log.d(TAG, "Datele au fost modificate cu succes!");
                    // Afiseaza un mesaj de succes
                    Toast.makeText(this, "Datele profesorului au fost actualizate cu succes!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to update professor details");
                }
            } else {
                Log.e(TAG, "Failed to connect to the database");
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception: " + e.getMessage());
        } finally {
            // Close the resources
            try {
                if (stmt != null) {
                    stmt.close();
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


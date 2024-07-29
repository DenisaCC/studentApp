package com.example.licentaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.licentaapp.Connection.ConnectionClass;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AccountDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView textViewUsername;
    private ImageView imageViewProfile;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        textViewUsername = findViewById(R.id.textViewUsername);
        imageViewProfile = findViewById(R.id.userImage);
        Intent intent = getIntent();

        if (intent != null) {
            username = intent.getStringExtra("USERNAME");
            textViewUsername.setText(username);
            loadStudentData(username);
        }

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(v -> finish());

        TextView changePhotoTextView = findViewById(R.id.signupText);
        changePhotoTextView.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        new UploadProfileImageTask().execute(imageUri);
    }

    private class UploadProfileImageTask extends AsyncTask<Uri, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... params) {
            Uri imageUri = params[0];
            Connection connection = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                // Convert the image URI to an InputStream
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = new byte[imageStream.available()];
                imageStream.read(imageBytes);
                imageStream.close();

                // Upload the image URI to the database
                String updateQuery = "UPDATE Utilizator SET ImagineProfil = ? WHERE NumeUtilizator = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, imageUri.toString()); // Salvează URI-ul imaginii în loc de byte[]
                preparedStatement.setString(2, username);
                int rowsAffected = preparedStatement.executeUpdate();
                preparedStatement.close();

                return rowsAffected > 0;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AccountDetailsActivity.this, "Imagine încărcată cu succes", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AccountDetailsActivity.this, "Încărcarea imaginii a eșuat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadStudentData(String username) {
        new LoadStudentDataTask().execute(username);
    }

    private class LoadStudentDataTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String username = params[0];
            Connection connection = null;
            String[] studentData = new String[10];
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                Statement statement = connection.createStatement();
                String query = "SELECT Nume, Prenume, Email, AnStudiu, NrMatricol FROM Student WHERE NrMatricol IN (SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = '" + username + "')";
                ResultSet resultSet = statement.executeQuery(query);

                Statement facultyStatement = connection.createStatement();
                String facultyQuery = "SELECT F.Denumire AS Denumire " +
                        "FROM Student S " +
                        "JOIN Utilizator U ON S.NrMatricol = U.NrMatricol " +
                        "JOIN Facultate F ON S.IDFacultate = F.IDFacultate " +
                        "WHERE U.NumeUtilizator = '" + username + "'";
                ResultSet facultyResultSet = facultyStatement.executeQuery(facultyQuery);

                // Interogare pentru a obține denumirea departamentului asociat ID-ului departamentului din tabelul Student
                Statement departmentStatement = connection.createStatement();
                String departmentQuery = "SELECT D.NumeDepartament AS NumeDepartament " +
                        "FROM Student S " +
                        "JOIN Utilizator U ON S.NrMatricol = U.NrMatricol " +
                        "JOIN Departament D ON S.IDDepartament = D.IDDepartament " +
                        "WHERE U.NumeUtilizator = '" + username + "'";
                ResultSet departmentResultSet = departmentStatement.executeQuery(departmentQuery);

                // Interogare pentru a obține denumirea specializarii asociat ID-ului specializarii din tabelul Student
                Statement specializationStatement = connection.createStatement();
                String specializationQuery = "SELECT Spec.Nume AS Nume " +
                        "FROM Student S " +
                        "JOIN Utilizator U ON S.NrMatricol = U.NrMatricol " +
                        "JOIN Specializare Spec ON S.IDSpecializare = Spec.IDSpecializare " +
                        "WHERE U.NumeUtilizator = '" + username + "'";
                ResultSet specializationResultSet = specializationStatement.executeQuery(specializationQuery);

                // Interogare pentru a obține numele grupei asociat ID-ului grupei din tabelul Student
                Statement grupaStatement = connection.createStatement();
                String grupaQuery = "SELECT G.Nume AS Nume " +
                        "FROM Student S " +
                        "JOIN Utilizator U ON S.NrMatricol = U.NrMatricol " +
                        "JOIN Grupa G ON S.IDGrupa = G.IDGrupa " +
                        "WHERE U.NumeUtilizator = '" + username + "'";
                ResultSet grupaResultSet = grupaStatement.executeQuery(grupaQuery);

                // Obținerea caii imaginii din baza de date
                Statement imagePathStatement = connection.createStatement();
                String imagePathQuery = "SELECT ImagineProfil FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                ResultSet imagePathResultSet = imagePathStatement.executeQuery(imagePathQuery);
                if (imagePathResultSet.next()) {
                    studentData[9] = imagePathResultSet.getString("ImagineProfil");
                }
                imagePathResultSet.close();
                imagePathStatement.close();

                if (resultSet.next()) {
                    studentData[0] = resultSet.getString("Nume");
                    studentData[1] = resultSet.getString("Prenume");
                    studentData[2] = resultSet.getString("Email");
                    studentData[6] = resultSet.getString("NrMatricol");
                    studentData[7] = resultSet.getString("AnStudiu");
                }
                // Extrageți denumirea facultății dacă există
                if (facultyResultSet.next()) {
                    studentData[3] = facultyResultSet.getString("Denumire");
                }

                if (departmentResultSet.next()) {
                    studentData[4] = departmentResultSet.getString("NumeDepartament");
                }

                if (specializationResultSet.next()) {
                    studentData[5] = specializationResultSet.getString("Nume");
                }

                if (grupaResultSet.next()) {
                    studentData[8] = grupaResultSet.getString("Nume");
                }

                // Închide resursele
                resultSet.close();
                statement.close();
                facultyResultSet.close();
                facultyStatement.close();
                departmentResultSet.close();
                departmentStatement.close();
                specializationResultSet.close();
                specializationStatement.close();
                grupaResultSet.close();
                grupaStatement.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return studentData;
        }

        @Override
        protected void onPostExecute(String[] studentData) {
            if (studentData != null) {
                TextView textViewNume = findViewById(R.id.textViewNumeValue);
                TextView textViewPrenume = findViewById(R.id.textViewPrenumeValue);
                TextView textViewEmail = findViewById(R.id.textViewEmailValue);
                TextView textViewDepartment = findViewById(R.id.textViewDepartamentValue);
                TextView textViewSpecialization = findViewById(R.id.textViewSpecializareValue);
                TextView textViewFaculty = findViewById(R.id.textViewFacultateValue);
                TextView textViewGroup = findViewById(R.id.textViewGrupaValue);
                TextView textViewYear = findViewById(R.id.textViewAnValue);
                TextView textViewNrMatricol = findViewById(R.id.textViewNrMatricolValue);

                textViewNume.setText(studentData[0]);
                textViewPrenume.setText(studentData[1]);
                textViewEmail.setText(studentData[2]);
                textViewDepartment.setText(studentData[4]);
                textViewSpecialization.setText(studentData[5]);
                textViewFaculty.setText(studentData[3]);
                textViewGroup.setText(studentData[8]);
                textViewYear.setText(studentData[7]);
                textViewNrMatricol.setText(studentData[6]);
                // Load the profile image
                ImageView imageViewProfile = findViewById(R.id.userImage);
                if (studentData[9] != null && !studentData[9].isEmpty()) {
                    Glide.with(AccountDetailsActivity.this)
                            .load(studentData[9])

                            .into(imageViewProfile);
                } else {
                    // Setează o imagine de rezervă în cazul în care nu există o imagine de profil
                    Glide.with(AccountDetailsActivity.this)
                            .load(R.drawable.loading)
                            .into(imageViewProfile);
                }
            }
        }

    }
}

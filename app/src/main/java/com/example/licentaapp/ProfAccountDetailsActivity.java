package com.example.licentaapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.LetterImageView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfAccountDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textViewNumeValue, textViewPrenumeValue, textViewEmailValue, textViewTitluValue, textViewFacultatiValue, textViewAdresaValue, textViewTelefonValue;
    private ImageView imageViewProfile;
    private Connection con;
    private int professorId;
    private static final int PICK_IMAGE = 1;
    private Bitmap bitmap;
    private TextView textViewUsernameValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_account_details);

        // Initialize TextViews and ImageView
        textViewNumeValue = findViewById(R.id.textViewNumeValue);
        textViewPrenumeValue = findViewById(R.id.textViewPrenumeValue);
        textViewEmailValue = findViewById(R.id.textViewEmailValue);
        textViewTitluValue = findViewById(R.id.textViewTitluValue);
        textViewFacultatiValue = findViewById(R.id.textViewFacultatiValue);
        textViewAdresaValue = findViewById(R.id.textViewAdresaValue);
        textViewTelefonValue = findViewById(R.id.textViewTelefonValue);
        imageViewProfile = findViewById(R.id.userImage); // or any appropriate ImageView
        textViewUsernameValue = findViewById(R.id.textViewUsername);

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        professorId = intent.getIntExtra("PROFESSOR_ID", -1);
        String username = intent.getStringExtra("USERNAME");

        if (professorId != -1) {
            new FetchProfessorDetailsTask().execute(professorId);
        } else {
            Toast.makeText(this, "Eroare: ID-ul profesorului este invalid", Toast.LENGTH_LONG).show();
        }

        // Set username text in TextView
        textViewUsernameValue.setText(username); // Assuming you have a textViewUsernameValue defined

        if (professorId != -1) {
            new FetchProfessorDetailsTask().execute(professorId);
        } else {
            Toast.makeText(this, "Eroare: ID-ul profesorului este invalid", Toast.LENGTH_LONG).show();
        }

        // Set onClickListener for changePhoto TextView
        TextView changePhotoTextView = findViewById(R.id.changePhoto);
        changePhotoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri); // Setează imaginea selectată în ImageView
            uploadProfileImage(imageUri); // Încarcă imaginea în baza de date
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

                // Încărcați calea imaginii în baza de date
                String updateQuery = "UPDATE UtilizatorProfesor SET ImagineProfil = ? WHERE IDProfesor = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, imageUri.toString()); // Salvează URI-ul imaginii
                preparedStatement.setInt(2, professorId);
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
                Toast.makeText(ProfAccountDetailsActivity.this, "Imagine încărcată cu succes", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfAccountDetailsActivity.this, "Încărcarea imaginii a eșuat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchProfessorDetailsTask extends AsyncTask<Integer, Void, ResultSet> {

        @Override
        protected ResultSet doInBackground(Integer... params) {
            int profId = params[0];
            con = connectionClass(ConnectionClass.un, ConnectionClass.pass, ConnectionClass.db, ConnectionClass.ip, ConnectionClass.port);
            ResultSet resultSet = null;
            if (con == null) {
                runOnUiThread(() -> Toast.makeText(ProfAccountDetailsActivity.this, "Verificați conexiunea la internet", Toast.LENGTH_LONG).show());
            } else {
                try {
                    String query = "SELECT * FROM Profesor WHERE IDProfesor = " + profId;
                    Statement stmt = con.createStatement();
                    resultSet = stmt.executeQuery(query);
                } catch (SQLException e) {
                    Log.e("SQL Error: ", e.getMessage());
                }
            }
            return resultSet;
        }

        @Override
        protected void onPostExecute(ResultSet rs) {
            try {
                if (rs != null && rs.next()) {
                    textViewNumeValue.setText(rs.getString("Nume"));
                    textViewPrenumeValue.setText(rs.getString("Prenume"));
                    textViewEmailValue.setText(rs.getString("Email"));
                    textViewTitluValue.setText(rs.getString("Titlu"));
                    textViewAdresaValue.setText(rs.getString("Adresa"));
                    textViewTelefonValue.setText(rs.getString("NrTelefon"));

                    // Fetch Facultati
                    int professorId = rs.getInt("IDProfesor");
                    new FetchFacultatiTask().execute(professorId);
                } else {
                    Toast.makeText(ProfAccountDetailsActivity.this, "Nu s-au găsit detalii pentru acest profesor", Toast.LENGTH_LONG).show();
                }
            } catch (SQLException e) {
                Log.e("Data Error: ", e.getMessage());
                Toast.makeText(ProfAccountDetailsActivity.this, "Eroare la preluarea datelor", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException e) {
                    Log.e("ResultSet Error: ", e.getMessage());
                }
            }
        }
    }

    private class FetchFacultatiTask extends AsyncTask<Integer, Void, ResultSet> {

        @Override
        protected ResultSet doInBackground(Integer... params) {
            int profId = params[0];
            ResultSet resultSet = null;
            try {
                String query = "SELECT f.Denumire " +
                        "FROM Facultate f " +
                        "JOIN Profesor_Facultate pf ON f.IDFacultate = pf.IDFacultate " +
                        "WHERE pf.IDProfesor = " + profId;
                Statement stmt = con.createStatement();
                resultSet = stmt.executeQuery(query);
            } catch (SQLException e) {
                Log.e("SQL Error: ", e.getMessage());
            }
            return resultSet;
        }

        @Override
        protected void onPostExecute(ResultSet rs) {
            try {
                if (rs != null && rs.next()) {
                    textViewFacultatiValue.setText(rs.getString("Denumire"));
                } else {
                    textViewFacultatiValue.setText("N/A");
                }
            } catch (SQLException e) {
                Log.e("Data Error: ", e.getMessage());
                Toast.makeText(ProfAccountDetailsActivity.this, "Eroare la preluarea detaliilor facultății", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException e) {
                    Log.e("ResultSet Error: ", e.getMessage());
                }
            }
        }
    }

    public Connection connectionClass(String user, String password, String db, String ip, String port) {
        Connection connection = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String ConnectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databasename=" + db + ";user=" + user + ";password=" + password + ";";
            connection = DriverManager.getConnection(ConnectURL);
        } catch (ClassNotFoundException | SQLException e) {
            Log.e("Connection Error: ", e.getMessage());
        }
        return connection;
    }
}

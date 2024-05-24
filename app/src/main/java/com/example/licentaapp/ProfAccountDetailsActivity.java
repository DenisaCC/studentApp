package com.example.licentaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.bumptech.glide.Glide;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProfAccountDetailsActivity extends AppCompatActivity {

    private TextView textViewProfUsername;
    private TextView textViewProfEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        textViewProfUsername = findViewById(R.id.textViewUsername);
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("USERNAME");
            textViewProfUsername.setText(username);
            loadStudentData(username);
        }
        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadStudentData(String username) {

        new LoadProfessorDataTask().execute(username);
    }

    private class LoadProfessorDataTask extends AsyncTask<String, Void, String[]> {
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
                String query = "SELECT Nume, Prenume, Email, Titlu FROM Student WHERE NrMatricol IN (SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = '" + username + "')";
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
                // Închide conexiunea la baza de date
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
            TextView textViewNumeValue = findViewById(R.id.textViewNumeValue);
            TextView textViewPrenumeValue = findViewById(R.id.textViewPrenumeValue);
            TextView textViewEmailValue = findViewById(R.id.textViewEmailValue);
            TextView textViewFacultateValue = findViewById(R.id.textViewFacultateValue);
            TextView textViewDepartamentValue = findViewById(R.id.textViewDepartamentValue);
            TextView textViewSpecializareValue = findViewById(R.id.textViewSpecializareValue);
            TextView textViewNrMatricol = findViewById(R.id.textViewNrMatricolValue);
            TextView textViewAnValue = findViewById(R.id.textViewAnValue);
            TextView textViewGrupaValue = findViewById(R.id.textViewGrupaValue);
            ImageView imageViewProfile = findViewById(R.id.userImage);

            if (studentData != null && studentData.length == 10) {
                textViewNumeValue.setText(studentData[0]);
                textViewPrenumeValue.setText(studentData[1]);
                textViewEmailValue.setText(studentData[2]);
                textViewFacultateValue.setText(studentData[3]);
                textViewDepartamentValue.setText(studentData[4]);
                textViewSpecializareValue.setText(studentData[5]);
                textViewNrMatricol.setText(studentData[6]);
                textViewAnValue.setText(studentData[7]);
                textViewGrupaValue.setText(studentData[8]);
                // Extragerea caii catre imaginea de profil din studentData
                String profileImagePath = studentData[9]; // Înlocuiți index_of_path_to_image cu indicele corespunzător din array-ul studentData
                // Verificarea și încărcarea imaginii de profil utilizând Glide
                if (profileImagePath != null && !profileImagePath.isEmpty()) {
                    Glide.with(ProfAccountDetailsActivity.this).load(Uri.parse(profileImagePath)).into(imageViewProfile);
                } else {
                    // Dacă calea imaginii este invalidă sau goală, afișați o imagine implicită
                }
            } else {
                // Gestionați cazul în care datele despre student nu sunt disponibile sau nu sunt corecte
            }
        }
    }
}
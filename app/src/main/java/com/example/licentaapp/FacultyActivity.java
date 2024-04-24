package com.example.licentaapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FacultyActivity extends AppCompatActivity {

    private int facultyID = -1;
    private List<Integer> departmentIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FacultyActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("USERNAME");
            if (username != null) {
                new GetFacultyDataTask().execute(username);
            }
        }
    }

    private class GetFacultyDataTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String username = params[0];
            String[] facultyData = new String[8];

            Connection connection = null;
            Statement studentStatement = null;
            ResultSet studentResultSet = null;
            Statement facultyStatement = null;
            ResultSet facultyResultSet = null;
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                studentStatement = connection.createStatement();
                String studentQuery = "SELECT S.IDFacultate, F.Denumire AS NumeFacultate " +
                        "FROM Utilizator U " +
                        "JOIN Student S ON U.NrMatricol = S.NrMatricol " +
                        "JOIN Facultate F ON S.IDFacultate = F.IDFacultate " +
                        "WHERE U.NumeUtilizator = '" + username + "'";
                studentResultSet = studentStatement.executeQuery(studentQuery);

                String facultyName = null;
                if (studentResultSet.next()) {
                    facultyID = studentResultSet.getInt("IDFacultate");
                    facultyName = studentResultSet.getString("NumeFacultate");
                }

                if (facultyID != -1 && facultyName != null) {
                    facultyStatement = connection.createStatement();
                    String facultyQuery = "SELECT Adresa, Email, Telefon, Decan, Prodecan " +
                            "FROM Facultate " +
                            "WHERE IDFacultate = " + facultyID;
                    facultyResultSet = facultyStatement.executeQuery(facultyQuery);
                    if (facultyResultSet.next()) {
                        facultyData[0] = facultyName;
                        facultyData[1] = facultyResultSet.getString("Decan");
                        facultyData[2] = facultyResultSet.getString("Prodecan");
                        facultyData[3] = facultyResultSet.getString("Adresa");
                        facultyData[4] = facultyResultSet.getString("Email");
                        facultyData[5] = facultyResultSet.getString("Telefon");

                        // Interogare pentru a obține adresa de email a decanului pentru acea facultate
                        String deanEmailQuery = "SELECT P.Email " +
                                "FROM Profesor_Facultate PF " +
                                "INNER JOIN Profesor P ON PF.IDProfesor = P.IDProfesor " +
                                "WHERE PF.IDFacultate = " + facultyID + " AND P.Titlu = 'Decan'";

                        Statement deanStatement = connection.createStatement();
                        ResultSet deanResultSet = deanStatement.executeQuery(deanEmailQuery);
                        if (deanResultSet.next()) {
                            String deanEmail = deanResultSet.getString("Email");
                            facultyData[6] = deanEmail;
                        }
                        deanResultSet.close();
                        deanStatement.close();

                        // Interogare pentru a obține adresa de email a prodecanului pentru acea facultate
                        String vicedeanEmailQuery = "SELECT P.Email " +
                                "FROM Profesor_Facultate PF " +
                                "INNER JOIN Profesor P ON PF.IDProfesor = P.IDProfesor " +
                                "WHERE PF.IDFacultate = " + facultyID + " AND P.Titlu = 'Prodecan'";

                        Statement vicedeanStatement = connection.createStatement();
                        ResultSet vicedeanResultSet = vicedeanStatement.executeQuery(vicedeanEmailQuery);
                        if (vicedeanResultSet.next()) {
                            String vicedeanEmail = vicedeanResultSet.getString("Email");
                            facultyData[7] = vicedeanEmail;
                        }
                        vicedeanResultSet.close();
                        vicedeanStatement.close();
                    }
                }
                Log.d("FacultyActivity", "Faculty ID: " + facultyID);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (facultyResultSet != null) {
                        facultyResultSet.close();
                    }
                    if (facultyStatement != null) {
                        facultyStatement.close();
                    }
                    if (studentResultSet != null) {
                        studentResultSet.close();
                    }
                    if (studentStatement != null) {
                        studentStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return facultyData;
        }

        @Override
        protected void onPostExecute(String[] facultyData) {
            ImageView imageView = findViewById(R.id.imageView);
            int imageResourceId = R.drawable.loading;
            switch (facultyID) {
                case 1:
                    imageResourceId = R.drawable.fbio;
                    break;
                case 2:
                    imageResourceId = R.drawable.fchimie;
                    break;
                // Alte cazuri
            }
            imageView.setImageResource(imageResourceId);

            TextView textViewFacultyName = findViewById(R.id.facultyName);
            TextView textViewDeanValue = findViewById(R.id.deanValue);
            TextView textViewVicedeanValue = findViewById(R.id.vicedeanValue);
            TextView textViewAddressValue = findViewById(R.id.addressValue);
            TextView textViewEmailValue = findViewById(R.id.emailValue);
            TextView textViewTelephoneValue = findViewById(R.id.telephoneValue);
            TextView textViewDeanEmailValue = findViewById(R.id.deanEmailValue);
            TextView textViewVicedeanEmailValue = findViewById(R.id.vicedeanEmailValue);

            if (facultyData != null && facultyData.length == 8) {
                textViewFacultyName.setText(facultyData[0]);
                textViewDeanValue.setText(facultyData[1]);
                textViewVicedeanValue.setText(facultyData[2]);
                textViewAddressValue.setText(facultyData[3]);
                textViewEmailValue.setText(facultyData[4]);
                textViewTelephoneValue.setText(facultyData[5]);
                textViewDeanEmailValue.setText(facultyData[6]);
                textViewVicedeanEmailValue.setText(facultyData[7]);
            } else {
                // Gestionează cazul în care nu se pot obține datele despre facultate
            }

            textViewEmailValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String emailAddress = textViewEmailValue.getText().toString();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + emailAddress));
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    } else {
                        Log.e("FacultyActivity", "Nu s-a găsit o aplicație de email pentru a deschide.");
                        Toast.makeText(FacultyActivity.this, "Nu s-a găsit o aplicație de email instalată.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            textViewTelephoneValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = textViewTelephoneValue.getText().toString();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    if (callIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(callIntent);
                    } else {
                        Log.e("FacultyActivity", "Nu s-a găsit o aplicație pentru a efectua un apel.");
                    }
                }
            });

            textViewDeanEmailValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String emailAddress = textViewDeanEmailValue.getText().toString();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + emailAddress));
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    } else {
                        Log.e("FacultyActivity", "Nu s-a găsit o aplicație de email pentru a deschide.");
                        Toast.makeText(FacultyActivity.this, "Nu s-a găsit o aplicație de email instalată.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            textViewVicedeanEmailValue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String emailAddress = textViewVicedeanEmailValue.getText().toString();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + emailAddress));
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    } else {
                        Log.e("FacultyActivity", "Nu s-a găsit o aplicație de email pentru a deschide.");
                        Toast.makeText(FacultyActivity.this, "Nu s-a găsit o aplicație de email instalată.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Obține și afișează numele departamentelor
            new GetDepartmentDataTask().execute();
        }
    }

    private class GetDepartmentDataTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> departmentNames = new ArrayList<>();
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                statement = connection.createStatement();
                String departmentQuery = "SELECT IDDepartament, NumeDepartament FROM Departament WHERE IDFacultate = " + facultyID;
                resultSet = statement.executeQuery(departmentQuery);

                while (resultSet.next()) {
                    departmentNames.add(resultSet.getString("NumeDepartament"));
                    departmentIDs.add(resultSet.getInt("IDDepartament"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return departmentNames;
        }

        @Override
        protected void onPostExecute(List<String> departmentNames) {
            ListView listView = findViewById(R.id.listview_department);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(FacultyActivity.this, android.R.layout.simple_list_item_1, departmentNames);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!departmentNames.isEmpty() && position < departmentNames.size()) {
                        String selectedDepartment = departmentNames.get(position);
                        int selectedDepartmentID = departmentIDs.get(position);

                        // Deschide o nouă activitate pentru a afișa specializările departamentului selectat
                        Intent intent = new Intent(FacultyActivity.this, SpecialisationActivity.class);
                        intent.putExtra("departmentName", selectedDepartment);
                        intent.putExtra("departmentID", selectedDepartmentID);
                        startActivity(intent);
                    }
                }
            });
        }

    }
}
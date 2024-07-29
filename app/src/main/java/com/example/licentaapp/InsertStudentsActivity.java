package com.example.licentaapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsertStudentsActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewFaculty;
    private AutoCompleteTextView autoCompleteTextViewDepartment;
    private AutoCompleteTextView autoCompleteTextViewSpecialisation;
    private TextInputEditText textInputEditTextCNP, textInputEditTextTelephone, textInputEditTextGroup, textInputEditTextDateOfBirth;
    private TextInputEditText textInputEditTextFirstName, textInputEditTextLastName, textInputEditTextAddress, textInputEditTextEmail, textInputEditTextYear, textInputEditTextNumber;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_students);

        autoCompleteTextViewFaculty = findViewById(R.id.autoCompleteTextViewFaculty);
        autoCompleteTextViewDepartment = findViewById(R.id.autoCompleteTextViewDepartment);
        autoCompleteTextViewSpecialisation = findViewById(R.id.autoCompleteTextViewSpecialisation);
        textInputEditTextCNP = findViewById(R.id.cnp);
        textInputEditTextTelephone = findViewById(R.id.phone);
        textInputEditTextGroup = findViewById(R.id.group);
        textInputEditTextFirstName = findViewById(R.id.firstName);
        textInputEditTextLastName = findViewById(R.id.lastName);
        textInputEditTextAddress = findViewById(R.id.address);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextYear = findViewById(R.id.year);
        textInputEditTextNumber = findViewById(R.id.number);

        populateFaculties();
        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());

        Button insertButton = findViewById(R.id.insertbtn);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCNPValid()) {
                    if (isTelephoneValid()) {
                        saveStudent();
                    } else {
                        Toast.makeText(InsertStudentsActivity.this, "Nr de telefon trebuie sa aiba exact 10 cifre.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InsertStudentsActivity.this, "CNP-ul trebuie să aibă exact 13 caractere numerice.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textInputEditTextDateOfBirth = findViewById(R.id.dateOfBirth);
        calendar = Calendar.getInstance();

        textInputEditTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        InsertStudentsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                updateLabel();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        autoCompleteTextViewFaculty.setOnItemClickListener((parent, view, position, id) -> populateDepartments());
        autoCompleteTextViewDepartment.setOnItemClickListener((parent, view, position, id) -> populateSpecialisations());
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; // Formatul de dată dorit
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        textInputEditTextDateOfBirth.setText(dateFormat.format(calendar.getTime()));
    }

    private boolean isCNPValid() {
        String cnp = textInputEditTextCNP.getText().toString();
        return cnp.length() == 13 && cnp.matches("\\d+");
    }

    private boolean isTelephoneValid() {
        String telephone = textInputEditTextTelephone.getText().toString();
        return telephone.length() == 10 && telephone.matches("\\d+");
    }

    private void saveStudent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                try {
                    conn = ConnectionClass.connect();
                    String facultyName = autoCompleteTextViewFaculty.getText().toString();
                    String departmentName = autoCompleteTextViewDepartment.getText().toString();
                    String specialisationName = autoCompleteTextViewSpecialisation.getText().toString();
                    String groupName = textInputEditTextGroup.getText().toString();
                    String cnp = textInputEditTextCNP.getText().toString();
                    String phone = textInputEditTextTelephone.getText().toString();
                    String dateOfBirth = textInputEditTextDateOfBirth.getText().toString(); // Date format: dd/MM/yyyy
                    String address = textInputEditTextAddress.getText().toString();
                    String email = textInputEditTextEmail.getText().toString();
                    String firstName = textInputEditTextFirstName.getText().toString();
                    String lastName = textInputEditTextLastName.getText().toString();
                    String year = textInputEditTextYear.getText().toString();
                    String number = textInputEditTextNumber.getText().toString();

                    int facultyId = getId(conn, "Facultate", "Denumire", facultyName);
                    int departmentId = getId(conn, "Departament", "NumeDepartament", departmentName);
                    int specialisationId = getId(conn, "Specializare", "Nume", specialisationName);
                    int groupId = getId(conn, "Grupa", "Nume", groupName);

                    // Format dateOfBirth to SQL Server compatible format: yyyy-MM-dd
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    Date date = sdf.parse(dateOfBirth);
                    sdf.applyPattern("yyyy-MM-dd");
                    dateOfBirth = sdf.format(date);

                    String insertQuery = "INSERT INTO Student (Nume, Prenume, CNP, DataNasterii, Adresa, Email, NrTelefon, IDFacultate, IDDepartament, IDSpecializare, AnStudiu, IDGrupa, NrMatricol) VALUES ('"
                            + firstName + "', '"
                            + lastName + "', '"
                            + cnp + "', '"
                            + dateOfBirth + "', '"
                            + address + "', '"
                            + email + "', '"
                            + phone + "', "
                            + facultyId + ", "
                            + departmentId + ", "
                            + specialisationId + ", '"
                            + year + "', "
                            + groupId + ", '"
                            + number + "')";

                    Statement statement = conn.createStatement();
                    statement.executeUpdate(insertQuery);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(InsertStudentsActivity.this, "Student inserat cu succes!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MyTag", "Eroare la inserarea studentului: " + e.getMessage());
                }
            }
        }).start();
    }


    private int getId(Connection conn, String tableName, String columnName, String value) throws Exception {
        String query = "SELECT ID" + tableName + " FROM " + tableName + " WHERE " + columnName + " = '" + value + "'";
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            throw new Exception("Nu s-a găsit " + columnName + " în " + tableName);
        }
    }

    private void populateFaculties() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> facultyNames = new ArrayList<>();
                Connection conn = null;

                try {
                    conn = ConnectionClass.connect();
                    Log.d("MyTag", "Conexiunea la baza de date a fost stabilită cu succes");

                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT Denumire FROM Facultate");
                    Log.d("MyTag", "Interogarea SELECT a fost executată cu succes");

                    while (resultSet.next()) {
                        facultyNames.add(resultSet.getString("Denumire"));
                        Log.d("MyTag", "Numele facultății: " + resultSet.getString("Denumire"));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(InsertStudentsActivity.this, android.R.layout.simple_dropdown_item_1line, facultyNames);
                            autoCompleteTextViewFaculty.setAdapter(adapter);
                        }
                    });

                    conn.close();
                    Log.d("MyTag", "Conexiunea la baza de date a fost închisă cu succes");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MyTag", "Eroare în timpul populării facultăților: " + e.getMessage());
                }
            }
        }).start();
    }

    private void populateDepartments() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> departments = new ArrayList<>();
                Connection conn = null;

                try {
                    conn = ConnectionClass.connect();
                    Log.d("MyTag", "Conexiunea la baza de date a fost stabilită cu succes");

                    String facultyName = autoCompleteTextViewFaculty.getText().toString();
                    int facultyId = getId(conn, "Facultate", "Denumire", facultyName);

                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT D.NumeDepartament FROM Departament D INNER JOIN Facultate F ON D.IDFacultate = F.IDFacultate WHERE F.IDFacultate = " + facultyId);
                    Log.d("MyTag", "Interogarea SELECT pentru departamente a fost executată cu succes");

                    while (resultSet.next()) {
                        departments.add(resultSet.getString("NumeDepartament"));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(InsertStudentsActivity.this, android.R.layout.simple_dropdown_item_1line, departments);
                            autoCompleteTextViewDepartment.setAdapter(adapter);
                        }
                    });

                    conn.close();
                    Log.d("MyTag", "Conexiunea la baza de date a fost închisă cu succes");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MyTag", "Eroare în timpul populării departamentelor: " + e.getMessage());
                }
            }
        }).start();
    }

    private void populateSpecialisations() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> specialisations = new ArrayList<>();
                Connection conn = null;

                try {
                    conn = ConnectionClass.connect();
                    Log.d("MyTag", "Conexiunea la baza de date a fost stabilită cu succes");

                    String departmentName = autoCompleteTextViewDepartment.getText().toString();
                    int departmentId = getId(conn, "Departament", "NumeDepartament", departmentName);

                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT S.Nume FROM Specializare S INNER JOIN Departament D ON S.IDDepartament = D.IDDepartament WHERE D.IDDepartament = " + departmentId);
                    Log.d("MyTag", "Interogarea SELECT pentru specializări a fost executată cu succes");

                    while (resultSet.next()) {
                        specialisations.add(resultSet.getString("Nume"));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(InsertStudentsActivity.this, android.R.layout.simple_dropdown_item_1line, specialisations);
                            autoCompleteTextViewSpecialisation.setAdapter(adapter);
                        }
                    });

                    conn.close();
                    Log.d("MyTag", "Conexiunea la baza de date a fost închisă cu succes");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MyTag", "Eroare în timpul populării specializărilor: " + e.getMessage());
                }
            }
        }).start();
    }
}

package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddNewsActivity extends AppCompatActivity {

    private TextView textViewDate, textViewFileSelected;
    private EditText editTextTitle, editTextDescription;
    private Button buttonSelectFile, buttonAddNews;
    private AutoCompleteTextView autoCompleteTextViewFaculty, autoCompleteTextViewDepartment, autoCompleteTextViewSpecialisation;
    private Uri fileUri;
    private byte[] fileData;
    private static final int FILE_SELECT_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        textViewDate = findViewById(R.id.textViewDate);
        textViewFileSelected = findViewById(R.id.textViewFileSelected);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSelectFile = findViewById(R.id.buttonSelectFile);
        buttonAddNews = findViewById(R.id.buttonAdd);
        autoCompleteTextViewFaculty = findViewById(R.id.autoCompleteTextViewFaculty);
        autoCompleteTextViewDepartment = findViewById(R.id.autoCompleteTextViewDepartment);
        autoCompleteTextViewSpecialisation = findViewById(R.id.autoCompleteTextViewSpecialisation);

        // Setează data curentă
        setCurrentDate();

        // Adaugă un click listener pentru a permite utilizatorului să aleagă o altă dată
        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        buttonAddNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNews();
            }
        });

        // Populează listele de facultăți, departamente și specializări
        populateFaculties();
        autoCompleteTextViewFaculty.setOnItemClickListener((parent, view, position, id) -> populateDepartments());
        autoCompleteTextViewDepartment.setOnItemClickListener((parent, view, position, id) -> populateSpecialisations());
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        textViewDate.setText(currentDate);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Updatează textul câmpului de text cu data selectată
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, monthOfYear, dayOfMonth);
                String selectedDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
                textViewDate.setText(selectedDateString);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selectați un fișier PDF"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            textViewFileSelected.setText(fileName);
            fileData = getFileData(fileUri);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private byte[] getFileData(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getId(Connection conn, String tableName, String columnName, String value) throws Exception {
        String query = "SELECT ID" + tableName + " FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new Exception("Nu s-a găsit " + columnName + " în " + tableName);
            }
        }
    }

    private void addNews() {
        String title = editTextTitle.getText().toString().trim();
        String date = textViewDate.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || description.isEmpty() || fileData == null) {
            Toast.makeText(this, "Toate câmpurile sunt obligatorii", Toast.LENGTH_SHORT).show();
            return;
        }

        Connection conn = ConnectionClass.connect();
        if (conn != null) {
            String query = "INSERT INTO Anunt (Titlu, Data, Descriere, PDF, IDFacultate, IDDepartament, IDSpecializare) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setDate(2, java.sql.Date.valueOf(date));
                stmt.setString(3, description);
                stmt.setBytes(4, fileData); // fileData este array-ul de bytes cu datele fișierului PDF
                stmt.setInt(5, getId(conn, "Facultate", "Denumire", autoCompleteTextViewFaculty.getText().toString()));
                stmt.setInt(6, getId(conn, "Departament", "NumeDepartament", autoCompleteTextViewDepartment.getText().toString()));
                stmt.setInt(7, getId(conn, "Specializare", "Nume", autoCompleteTextViewSpecialisation.getText().toString()));
                stmt.executeUpdate();
                Toast.makeText(this, "Anunțul a fost adăugat cu succes", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Eroare la adăugarea anunțului", Toast.LENGTH_SHORT).show();
            } finally {
                ConnectionClass.closeConnection(conn);
            }
        } else {
            Toast.makeText(this, "Eroare la conectarea la baza de date", Toast.LENGTH_SHORT).show();
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNewsActivity.this, android.R.layout.simple_dropdown_item_1line, facultyNames);
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNewsActivity.this, android.R.layout.simple_dropdown_item_1line, departments);
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddNewsActivity.this, android.R.layout.simple_dropdown_item_1line, specialisations);
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


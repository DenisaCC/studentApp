package com.example.licentaapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Message;
import android.se.omapi.Session;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.GmailSender;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class InsertProfessorsActivity extends AppCompatActivity {

    private TextInputEditText firstName, lastName, textInputEditTextCNP, textInputEditTextDateOfBirth, address, email, phone, title;
    private MultiAutoCompleteTextView multiAutoCompleteTextViewFaculty;
    private ChipGroup chipGroup;
    private Button insertBtn;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_professors);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        textInputEditTextCNP = findViewById(R.id.cnp);
        address = findViewById(R.id.address);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        title = findViewById(R.id.title);
        multiAutoCompleteTextViewFaculty = findViewById(R.id.multiAutoCompleteTextViewFaculty);
        chipGroup = findViewById(R.id.chipGroup);
        insertBtn = findViewById(R.id.insertbtn);
        // Populate the faculties for multi selection
        populateFaculties();

        Button insertButton = findViewById(R.id.insertbtn);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areFieldsValid()) {
                    if (isCNPValid()) {
                        if (isTelephoneValid()) {
                            saveProfessor();
                        } else {
                            Toast.makeText(InsertProfessorsActivity.this, "Nr de telefon trebuie sa aiba exact 10 cifre.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(InsertProfessorsActivity.this, "CNP-ul trebuie să aibă exact 13 caractere numerice.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                        InsertProfessorsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                updateLabel();
                            }
                        }, year, month, day);

                // Set the maximum date to today
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        multiAutoCompleteTextViewFaculty.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFaculty = (String) parent.getItemAtPosition(position);
            Log.d("MyTag", "Facultate selectată: " + selectedFaculty);
            addChipToGroup(selectedFaculty);
            updateMultiAutoCompleteTextView();
        });


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
        String telephone = phone.getText().toString();
        return telephone.length() == 10 && telephone.matches("\\d+");
    }
    private void addChipToGroup(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            updateMultiAutoCompleteTextView();
        });
        chipGroup.addView(chip);
    }

    private void updateMultiAutoCompleteTextView() {
        List<String> selectedFaculties = new ArrayList<>();
        StringBuilder facultiesSelected = new StringBuilder();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            String facultyName = chip.getText().toString();
            selectedFaculties.add(facultyName); // Adaugă numele facultății în lista selectedFaculties
            facultiesSelected.append(facultyName).append(", ");
        }
        if (facultiesSelected.length() > 0) {
            facultiesSelected.setLength(facultiesSelected.length() - 2); // remove last comma and space
        }
        multiAutoCompleteTextViewFaculty.setText(facultiesSelected.toString());
        multiAutoCompleteTextViewFaculty.setSelection(facultiesSelected.length());

        // Trimite lista de facultăți selectate către populateFaculties
        populateFaculties();
    }


    private void populateFaculties() {

        new Thread(() -> {
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
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(InsertProfessorsActivity.this, android.R.layout.simple_dropdown_item_1line, facultyNames);
                    multiAutoCompleteTextViewFaculty.setAdapter(adapter);
                    multiAutoCompleteTextViewFaculty.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                });
                conn.close();
                Log.d("MyTag", "Conexiunea la baza de date a fost închisă cu succes");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MyTag", "Eroare în timpul populării facultăților: " + e.getMessage());
            }
        }).start();
    }

    private void saveProfessor() {
        new Thread(() -> {
            Connection conn = null;
            try {
                // Retrieve data from the form
                String nume = firstName.getText().toString();
                String prenume = lastName.getText().toString();
                String cnpValue = textInputEditTextCNP.getText().toString();
                String dataNasterii = textInputEditTextDateOfBirth.getText().toString();
                String adresa = address.getText().toString();
                String emailValue = email.getText().toString();
                String nrTelefon = phone.getText().toString();
                String titlu = title.getText().toString();

                conn = ConnectionClass.connect();
                if (conn != null) {
                    // Check if email already exists
                    String checkEmailQuery = "SELECT COUNT(*) FROM UtilizatorProfesor WHERE Email = ?";
                    PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery);
                    checkEmailStmt.setString(1, emailValue);
                    ResultSet emailResultSet = checkEmailStmt.executeQuery();
                    if (emailResultSet.next() && emailResultSet.getInt(1) > 0) {
                        // Email already exists, show error message and exit
                        runOnUiThread(() -> Toast.makeText(InsertProfessorsActivity.this, "Există deja un cont cu acest email asociat", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // Insert data into the Professor table
                    String insertProfessorQuery = "INSERT INTO Profesor (Nume, Prenume, CNP, DataNasterii, Email, Adresa, NrTelefon, Titlu) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = conn.prepareStatement(insertProfessorQuery, Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setString(1, nume);
                    preparedStatement.setString(2, prenume);
                    preparedStatement.setString(3, cnpValue);
                    // Validate date format
                    if (dataNasterii.matches("\\d{2}/\\d{2}/\\d{4}")) {
                        // Convert date to SQL accepted format
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date date = sdf.parse(dataNasterii);
                        preparedStatement.setDate(4, new java.sql.Date(date.getTime()));
                    } else {
                        throw new IllegalArgumentException("Invalid date of birth format. Please use dd/MM/yyyy format.");
                    }
                    preparedStatement.setString(5, emailValue);
                    preparedStatement.setString(6, adresa);
                    preparedStatement.setString(7, nrTelefon);
                    preparedStatement.setString(8, titlu);

                    int affectedRows = preparedStatement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Crearea unui cont pentru profesor a eșuat!");
                    }

                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long professorId = generatedKeys.getLong(1);
                            // Get selected faculties
                            List<String> selectedFaculties = new ArrayList<>();
                            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                                Chip chip = (Chip) chipGroup.getChildAt(i);
                                selectedFaculties.add(chip.getText().toString());
                            }

                            // Insert data into the Professor_Faculty table
                            for (String facultyName : selectedFaculties) {
                                String selectFacultyQuery = "SELECT IDFacultate FROM Facultate WHERE Denumire = ?";
                                PreparedStatement selectFacultyStmt = conn.prepareStatement(selectFacultyQuery);
                                selectFacultyStmt.setString(1, facultyName);
                                ResultSet facultyResultSet = selectFacultyStmt.executeQuery();
                                if (facultyResultSet.next()) {
                                    long facultyId = facultyResultSet.getLong("IDFacultate");

                                    String insertProfessorFacultyQuery = "INSERT INTO Profesor_Facultate (IDProfesor, IDFacultate) VALUES (?, ?)";
                                    PreparedStatement insertProfessorFacultyStmt = conn.prepareStatement(insertProfessorFacultyQuery);
                                    insertProfessorFacultyStmt.setLong(1, professorId);
                                    insertProfessorFacultyStmt.setLong(2, facultyId);
                                    insertProfessorFacultyStmt.executeUpdate();
                                }
                            }

                            // Generate username and password
                            String username = generateUsername(nume, prenume);
                            String password = generatePassword(8);

                            // Save professor account in the UtilizatorProfesor table
                            String insertUserQuery = "INSERT INTO UtilizatorProfesor (NumeUtilizator, Email, Parola, IDProfesor) VALUES (?, ?, ?, ?)";
                            PreparedStatement insertUserStmt = conn.prepareStatement(insertUserQuery);
                            insertUserStmt.setString(1, username);
                            insertUserStmt.setString(2, emailValue);
                            insertUserStmt.setString(3, password);
                            insertUserStmt.setLong(4, professorId);
                            insertUserStmt.executeUpdate();

                            // Send email with generated password to professor
                            String emailSubject = "Creare Cont - Profesor";
                            String emailContent = "Stimate Profesor,\n\nContul dumneavoastră pentru aplicația StudentApp a fost creat cu succes. "
                                    + "Numele dumneavoastră de utilizator este: " + username + "\nParola generată este: " + password
                                    + "\n\nVă rugăm să vă autentificați cu aceste informații și să schimbați parola după prima logare."
                                    + "\n\nCu stimă,\nEchipa StudentApp";

                            GmailSender.sendEmail(emailValue, emailSubject, emailContent);

                            // Show success messages for adding professor and sending email
                            runOnUiThread(() -> {
                                Toast.makeText(InsertProfessorsActivity.this, "Professor added successfully! Email sent with login information.", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            throw new SQLException("Creating professor failed, no generated key obtained.");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(InsertProfessorsActivity.this, "Error adding professor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (ParseException | IllegalArgumentException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(InsertProfessorsActivity.this, "Error converting data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(InsertProfessorsActivity.this, "Error sending email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    private String generateUsername(String firstName, String lastName) {
        // Transformă prima literă în majusculă și restul în minusculă pentru prenume
        String formattedFirstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
        // Transformă prima literă în majusculă și restul în minusculă pentru nume
        String formattedLastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

        // Concatenare prenume și nume cu underline
        return formattedFirstName + "_" + formattedLastName;
    }

    private boolean areFieldsValid() {
        // Verificare completitudine câmpuri
        boolean isValid = true;
        if (firstName.getText().toString().trim().isEmpty()) {
            firstName.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastName.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (textInputEditTextCNP.getText().toString().trim().isEmpty()) {
            textInputEditTextCNP.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (textInputEditTextDateOfBirth.getText().toString().trim().isEmpty()) {
            textInputEditTextDateOfBirth.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (address.getText().toString().trim().isEmpty()) {
            address.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (email.getText().toString().trim().isEmpty()) {
            email.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (phone.getText().toString().trim().isEmpty()) {
            phone.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (title.getText().toString().trim().isEmpty()) {
            title.setError("Acest câmp este obligatoriu.");
            isValid = false;
        }
        if (chipGroup.getChildCount() == 0) {
            Toast.makeText(InsertProfessorsActivity.this, "Trebuie să selectați cel puțin o facultate.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    private String generatePassword(int length) {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String combinedChars = upperCaseLetters + lowerCaseLetters + numbers;
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);

        // Garantăm cel puțin o majusculă și un număr
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        // Completăm restul parolei
        for (int i = 2; i < length; i++) {
            password.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        return password.toString();
    }

}


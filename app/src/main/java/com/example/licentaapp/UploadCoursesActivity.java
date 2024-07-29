package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

public class UploadCoursesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_EXCEL_FILE = 123;
    private Uri excelFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_courses);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> {
            // Întoarcere la activitatea principală
            finish();
        });

        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setOnClickListener(v -> {
            // Lansăm selectorul de fișiere pentru a permite utilizatorului să aleagă un fișier Excel
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_EXCEL_FILE);
        });

        Button uploadDataButton = findViewById(R.id.uploadToSSMSButton);
        uploadDataButton.setOnClickListener(v -> {
            if (excelFileUri != null) {
                // Procesăm fișierul Excel și încărcăm datele în SSMS
                processCoursesExcel(excelFileUri);
            } else {
                Toast.makeText(this, "Vă rugăm să selectați un fișier Excel mai întâi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            // Utilizatorul a selectat un fișier Excel
            excelFileUri = data.getData();
            // Actualizăm textul butonului cu numele fișierului
            updateChooseFileButton(excelFileUri);
        }
    }

    private void processCoursesExcel(Uri uri) {
        Connection conn = null;

        try {
            conn = ConnectionClass.connect();
            if (conn != null) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                // Șterge înregistrările asociate din tabela Disciplina_Specializare
                String deleteOrarQuery = "DELETE FROM Orar WHERE IDDisciplina IN (SELECT IDDisciplina FROM Disciplina)";
                PreparedStatement deleteOrarStatement = conn.prepareStatement(deleteOrarQuery);
                deleteOrarStatement.executeUpdate();

                String deleteDisciplinaQuery = "DELETE FROM Disciplina";
                PreparedStatement deleteDisciplinaStatement = conn.prepareStatement(deleteDisciplinaQuery);
                deleteDisciplinaStatement.executeUpdate();

// Optionally, reseed the identity column for Disciplina table if necessary
                String reseedQuery = "DBCC CHECKIDENT('Disciplina', RESEED, 0)";
                PreparedStatement reseedStatement = conn.prepareStatement(reseedQuery);
                reseedStatement.executeUpdate();


                for (Row row : sheet) {
                    if (row.getRowNum() > 0) {
                        String numeDisciplina = getStringCellValue(row.getCell(0));
                        String descriereDisciplina = getStringCellValue(row.getCell(1));
                        String numeCompletProfesor = getStringCellValue(row.getCell(2));
                        String numeFacultate = getStringCellValue(row.getCell(3));
                        String numeDepartament = getStringCellValue(row.getCell(4));
                        String numeSpecializare = getStringCellValue(row.getCell(5));

                        try {
                            String[] numePrenumeProfesor = numeCompletProfesor.split(" ");
                            if (numePrenumeProfesor.length < 2) {
                                Log.e("Excel Processing Error", "Invalid professor name format: " + numeCompletProfesor);
                                continue;
                            }
                            String nume = numePrenumeProfesor[numePrenumeProfesor.length - 1];
                            String prenume = String.join(" ", Arrays.copyOf(numePrenumeProfesor, numePrenumeProfesor.length - 1));

                            int idDepartament = getDepartmentID(numeDepartament, conn);
                            int idProfesor = getProfessorID(numeCompletProfesor, conn);
                            int idFacultate = getFacultyID(numeFacultate, conn);
                            int idSpecializare = getSpecialisationID(numeSpecializare, conn);

                            Log.d("Excel Processing", "Disciplina: " + numeDisciplina + ", Descriere: " + descriereDisciplina + ", ID Departament: " + idDepartament + ", ID Profesor: " + idProfesor + ", ID Facultate: " + idFacultate + ", ID Specializare: " + idSpecializare);

                            if (idDepartament == -1 || idProfesor == -1 || idFacultate == -1 || idSpecializare == -1) {
                                Log.e("Excel Processing Error", "Invalid foreign key detected. Skipping insertion.");
                                continue;
                            }

                            // Verificăm dacă disciplina există deja înainte de a o insera
                            int disciplineID = getDisciplineID(numeDisciplina, conn);
                            if (disciplineID == -1) {
                                // Dacă nu există, o inserăm în tabela Disciplina
                                String insertQuery = "INSERT INTO Disciplina (Nume, Descriere, IDDepartament, IDProfesor, IDFacultate, IDSpecializare) VALUES (?, ?, ?, ?, ?, ?)";
                                PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
                                insertStatement.setString(1, numeDisciplina);
                                insertStatement.setString(2, descriereDisciplina);
                                insertStatement.setInt(3, idDepartament);
                                insertStatement.setInt(4, idProfesor);
                                insertStatement.setInt(5, idFacultate);
                                insertStatement.setInt(6, idSpecializare);
                                insertStatement.executeUpdate();

                                // Obținem ID-ul disciplinii inserate
                                disciplineID = getDisciplineID(numeDisciplina, conn);
                            } else {
                                Log.d("Excel Processing", "Disciplina already exists: " + numeDisciplina);
                            }

                            // Salvăm asocierea dintre disciplină și specializare în tabela Disciplina_Specializare
                            String associationQuery = "INSERT INTO Disciplina_Specializare (IDDisciplina, IDSpecializare) VALUES (?, ?)";
                            PreparedStatement associationStatement = conn.prepareStatement(associationQuery);
                            associationStatement.setInt(1, disciplineID);
                            associationStatement.setInt(2, idSpecializare);
                            associationStatement.executeUpdate();

                        } catch (Exception e) {
                            Log.e("Excel Processing Error", "Error processing Excel file", e);
                            Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                workbook.close();
                inputStream.close();
                Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Excel Processing Error", "Error processing Excel file", e);
            Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_SHORT).show();
        } finally {
            ConnectionClass.closeConnection(conn);
        }
    }


    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private int getDepartmentID(String departmentName, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDDepartament FROM Departament WHERE NumeDepartament = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, departmentName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDDepartament");
            }
        } catch (Exception e) {
            Log.e("Get Department ID Error", "Error getting Department ID", e);
        }
        return id;
    }

    public int getProfessorID(String fullName, Connection conn) {
        int professorID = -1;

        try {
            String[] nameParts = fullName.split("\\s+");
            String firstName = nameParts[0];
            String lastName = nameParts[nameParts.length - 1];

            String query = "SELECT IDProfesor FROM Profesor WHERE Nume = ? AND Prenume = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                professorID = rs.getInt("IDProfesor");
            }
        } catch (Exception e) {
            Log.e("Get Professor ID Error", e.getMessage());
        }

        return professorID;
    }

    private int getFacultyID(String facultyName, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDFacultate FROM Facultate WHERE Denumire = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, facultyName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDFacultate");
            }
        } catch (Exception e) {
            Log.e("Get Faculty ID Error", "Error getting Faculty ID", e);
        }
        return id;
    }

    private int getSpecialisationID(String specialisationName, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDSpecializare FROM Specializare WHERE Nume = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, specialisationName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDSpecializare");
            }
        } catch (Exception e) {
            Log.e("Get Specialisation ID Error", "Error getting Specialisation ID", e);
        }
        return id;
    }

    private int getDisciplineID(String disciplineName, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDDisciplina FROM Disciplina WHERE Nume = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, disciplineName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDDisciplina");
            }
        } catch (Exception e) {
            Log.e("Get Discipline ID Error", "Error getting Discipline ID", e);
        }
        return id;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateChooseFileButton(Uri uri) {
        String fileName = getFileName(uri);
        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setText(fileName);
    }
}

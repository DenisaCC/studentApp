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
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class UploadScheduleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_EXCEL_FILE = 123;
    private Uri excelFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_schedule);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(v -> finish());

        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_EXCEL_FILE);
        });

        Button uploadDataButton = findViewById(R.id.uploadToSSMSButton);
        uploadDataButton.setOnClickListener(v -> {
            if (excelFileUri != null) {
                processExcelFile(excelFileUri);
            } else {
                Toast.makeText(this, "Vă rugăm să selectați un fișier Excel mai întâi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            excelFileUri = data.getData();
            updateChooseFileButton(excelFileUri);
        }
    }

    private void processExcelFile(Uri uri) {
        Connection conn = ConnectionClass.connect();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

            for (Row row : sheet) {
                if (row.getRowNum() > 0) { // Ignorăm rândul de antet
                    String ziSaptamana = row.getCell(0).getStringCellValue();
                    String paritateSaptamana = row.getCell(1).getStringCellValue();
                    String oraInceputString = getStringCellValue(row.getCell(2));
                    String oraSfarsitString = getStringCellValue(row.getCell(3));
                    Time oraInceput = parseTime(oraInceputString, timeFormatter);
                    Time oraSfarsit = parseTime(oraSfarsitString, timeFormatter);
                    String disciplina = row.getCell(4).getStringCellValue();
                    String profesor = row.getCell(5).getStringCellValue();
                    String grupa = row.getCell(6).getStringCellValue();
                    String sala = row.getCell(7).getStringCellValue();
                    String tipActivitate = row.getCell(8).getStringCellValue();

                    int idDisciplina = getDisciplineId(disciplina, conn);
                    int idProfesor = getProfessorId(profesor, conn);
                    int idGrupa = getGroupId(grupa, conn);

                    if (idDisciplina != -1 && idProfesor != -1 && idGrupa != -1) {
                        String query = "INSERT INTO Orar (ZiSaptamana, ParitateSaptamana, OraInceput, OraSfarsit, IDDisciplina, IDProfesor, IDGrupa, Sala, TipActivitate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement statement = conn.prepareStatement(query);
                        statement.setString(1, ziSaptamana);
                        statement.setString(2, paritateSaptamana);
                        statement.setTime(3, oraInceput);
                        statement.setTime(4, oraSfarsit);
                        statement.setInt(5, idDisciplina);
                        statement.setInt(6, idProfesor);
                        statement.setInt(7, idGrupa);
                        statement.setString(8, sala);
                        statement.setString(9, tipActivitate);
                        statement.executeUpdate();
                    } else {
                        // Tratarea cazului în care nu s-au găsit ID-uri asociate numelor din fișierul Excel
                        Log.e("Insert Error", "Nu s-au găsit ID-uri asociate pentru datele din fișierul Excel: " + disciplina + ", " + profesor + ", " + grupa);
                    }
                }
            }

            workbook.close();
            inputStream.close();
            Toast.makeText(this, "Orarul a fost încărcat cu succes!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Excel Processing Error", "Eroare la procesarea fișierului Excel", e);
            Toast.makeText(this, "Eroare la procesarea fișierului Excel.", Toast.LENGTH_SHORT).show();
        } finally {
            ConnectionClass.closeConnection(conn);
        }
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
                cursor.close();
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
    private Time parseTime(String timeString, SimpleDateFormat formatter) {
        try {
            // Try to parse using the provided formatter
            Date date = formatter.parse(timeString);
            return new Time(date.getTime());
        } catch (Exception e) {
            try {
                // Try to parse as a double value representing a fraction of the day
                double decimalTime = Double.parseDouble(timeString);
                int totalSeconds = (int) (decimalTime * 24 * 60 * 60);
                int hours = totalSeconds / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;
                return new Time(hours, minutes, seconds);
            } catch (NumberFormatException e2) {
                Log.e("UploadProfProgramActivity", "Eroare la parsarea valorii timpului: " + timeString, e2);
                return null;
            }
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
                // Check if the numeric cell is formatted as a date/time
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return new SimpleDateFormat("HH:mm:ss").format(date);
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            return new SimpleDateFormat("HH:mm:ss").format(date);
                        }
                        return String.valueOf(cell.getNumericCellValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    private void updateChooseFileButton(Uri uri) {
        String fileName = getFileName(uri);
        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setText(fileName);
    }

    private int getDisciplineId(String disciplineName, Connection conn) {
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

    public int getProfessorId(String fullName, Connection conn) {
        int professorID = -1; // Valoare implicită pentru cazul în care nu se găsește niciun profesor

        try {
            conn = ConnectionClass.connect(); // Obține conexiunea

            // Descompune numele complet în nume și prenume
            String[] nameParts = fullName.split("\\s+");
            String firstName = nameParts[0];
            String lastName = nameParts[nameParts.length - 1]; // Folosim ultimul element ca prenume

            // Afisam numele si prenumele pentru a verifica corectitudinea extragerii
            Log.d("Professor Name", "First Name: " + firstName + ", Last Name: " + lastName);

            // Interogare pentru a obține ID-ul profesorului
            String query = "SELECT IDProfesor FROM Profesor WHERE Nume = ? AND Prenume = ?";
            Log.d("Query", "Generated Query: " + query); // Afisam query-ul generat pentru debugging
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();

            // Dacă găsim un rând, extragem ID-ul profesorului
            if (rs.next()) {
                professorID = rs.getInt("IDProfesor");
            }
        } catch (SQLException e) {
            Log.e("Get Professor ID Error", e.getMessage());
        } finally {
            if (conn != null) {
                ConnectionClass.closeConnection(conn); // Închide conexiunea
            }
        }

        return professorID;
    }

    private int getGroupId(String groupName, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDGrupa FROM Grupa WHERE Nume = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, groupName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDGrupa");
            }
        } catch (Exception e) {
            Log.e("Get Group ID Error", "Error getting Group ID", e);
        }
        return id;
    }

}

package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import com.example.licentaapp.Connection.ConnectionClass;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadProfProgramActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_EXCEL_FILE = 123;
    private Uri excelFileUri;
    private Button selectFileButton, uploadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_prof_program);

        selectFileButton = findViewById(R.id.chooseFileButton);
        uploadFileButton = findViewById(R.id.button_upload_to_SSMS);

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

        Button uploadDataButton = findViewById(R.id.button_upload_to_SSMS);
        uploadDataButton.setOnClickListener(v -> {
            if (excelFileUri != null) {
                // Procesăm fișierul Excel și încărcăm datele în SSMS
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
            // Utilizatorul a selectat un fișier Excel
            excelFileUri = data.getData();
            // Actualizăm textul butonului cu numele fișierului
            updateChooseFileButton(excelFileUri);
        }
    }

    private void processExcelFile(Uri uri) {
        Connection conn = ConnectionClass.connect();
        if (conn == null) {
            Log.e("UploadProfProgramActivity", "Conexiunea la baza de date a eșuat.");
            return;
        }

        try {
            Log.d("UploadProfProgramActivity", "Procesează fișierul Excel...");
            String deleteQuery = "DELETE FROM ProgramProfesor";
            String reseedQuery = "DBCC CHECKIDENT('ProgramProfesor', RESEED, 0)";
            PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery);
            deleteStatement.executeUpdate();
            PreparedStatement reseedStatement = conn.prepareStatement(reseedQuery);
            reseedStatement.executeUpdate();

            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) { // Dacă acesta este primul rând, săriți peste el și continuați
                    continue;
                }

                String numeProfesor = getStringCellValue(row.getCell(0));
                int idProfesor = (int) getNumericCellValue(row.getCell(1));
                String materie = getStringCellValue(row.getCell(2));
                String tipActivitate = getStringCellValue(row.getCell(3));
                String paritateSaptamana = getStringCellValue(row.getCell(4));
                String ziuaSaptamana = getStringCellValue(row.getCell(5));
                String oraInceputString = getStringCellValue(row.getCell(6));
                String oraSfarsitString = getStringCellValue(row.getCell(7));

                Time oraInceput = parseTime(oraInceputString, timeFormatter);
                Time oraSfarsit = parseTime(oraSfarsitString, timeFormatter);

                String sala = getStringCellValue(row.getCell(8));
                String facultate = getStringCellValue(row.getCell(9));
                String departament = getStringCellValue(row.getCell(10));
                String specializare = getStringCellValue(row.getCell(11));
                int anDeStudiu = (int) getNumericCellValue(row.getCell(12));
                String grupa = getStringCellValue(row.getCell(13));
                Log.d("UploadProfProgramActivity", "Date extrase: Nume Profesor: " + numeProfesor + ", ID Profesor: " + idProfesor );

                String query = "INSERT INTO ProgramProfesor (NumeProfesor, IdProfesor, Materie, TipActivitate, ParitateSaptamana, ZiuaSaptamânii, OraInceput, OraSfarsit, Sala, Facultate, Departament, Specializare, AnDeStudiu, Grupa) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, numeProfesor);
                statement.setInt(2, idProfesor);
                statement.setString(3, materie);
                statement.setString(4, tipActivitate);
                statement.setString(5, paritateSaptamana);
                statement.setString(6, ziuaSaptamana);
                statement.setTime(7, oraInceput);
                statement.setTime(8, oraSfarsit);
                statement.setString(9, sala);
                statement.setString(10, facultate);
                statement.setString(11, departament);
                statement.setString(12, specializare);
                statement.setInt(13, anDeStudiu);
                statement.setString(14, grupa);

                statement.executeUpdate();
                Log.d("UploadProfProgramActivity", "Datele din rândul " + row.getRowNum() + " au fost inserate cu succes în baza de date.");
            }

            workbook.close();
            inputStream.close();

            Log.d("UploadProfProgramActivity", "Datele au fost încărcate cu succes în baza de date!");
            Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Logăm eroarea în logcat
            Log.e("UploadProfProgramActivity", "Eroare la procesarea fișierului Excel:", e);
            Toast.makeText(this, "A apărut o eroare la procesarea fișierului.", Toast.LENGTH_LONG).show();
        } finally {
            ConnectionClass.closeConnection(conn);
        }
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

    private double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            default:
                return 0.0;
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

    private void updateChooseFileButton(Uri uri) {
        String fileName = getFileName(uri);
        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setText(fileName);
    }
}

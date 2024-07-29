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

public class UploadTaxesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_EXCEL_FILE = 123;
    private Uri excelFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_taxes);

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

        try {
            String deleteQuery = "DELETE FROM TaxeStudenti";
            String reseedQuery = "DBCC CHECKIDENT('TaxeStudenti', RESEED, 0)";
            PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery);
            deleteStatement.executeUpdate();
            PreparedStatement reseedStatement = conn.prepareStatement(reseedQuery);
            reseedStatement.executeUpdate();

            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() > 1) { // Verificăm dacă este un rând valid (ignorăm rândurile de antet)

                    // Extragem valorile din coloanele corespunzătoare
                    String facultate = getStringCellValue(row.getCell(0));
                    String departament = getStringCellValue(row.getCell(1));
                    String specializare = getStringCellValue(row.getCell(2));
                    int anDeStudii = (int) row.getCell(3).getNumericCellValue();
                    String grupa = getStringCellValue(row.getCell(4));
                    String numeStudent = getStringCellValue(row.getCell(5));
                    String prenumeStudent = getStringCellValue(row.getCell(6));
                    int nrMatricol = (int) row.getCell(7).getNumericCellValue();
                    double taxaCazareLunaAnterioara = getNumericCellValue(row.getCell(8));
                    double taxaCazareLunaInCurs = getNumericCellValue(row.getCell(9));
                    double taxaScolarizareSemestrul1 = getNumericCellValue(row.getCell(10));
                    double taxaScolarizareSemestrul2 = getNumericCellValue(row.getCell(11));

                    // Obținem ID-urile asociate pentru facultate, departament, specializare și grupă
                    int idFacultate = getFacultateId(facultate, conn);
                    int idDepartament = getDepartamentId(departament, conn);
                    int idSpecializare = getSpecializareId(specializare, conn);
                    int idGrupa = getGrupaId(grupa, conn);

                    // Construim și executăm instrucțiunea SQL pentru inserarea datelor în baza de date
                    String query = "INSERT INTO TaxeStudenti (IDFacultate, IDDepartament, IDSpecializare, AnDeStudii, IDGrupa, NrMatricol, TaxaCazareLunaInCurs, TaxaCazareLunaAnterioara, TaxaScolarizareSemestrul1, TaxaScolarizareSemestrul2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setInt(1, idFacultate);
                    statement.setInt(2, idDepartament);
                    statement.setInt(3, idSpecializare);
                    statement.setInt(4, anDeStudii);
                    statement.setInt(5, idGrupa);
                    statement.setInt(6, nrMatricol);
                    statement.setDouble(7, taxaCazareLunaInCurs);
                    statement.setDouble(8, taxaCazareLunaAnterioara);
                    statement.setDouble(9, taxaScolarizareSemestrul1);
                    statement.setDouble(10, taxaScolarizareSemestrul2);
                    statement.executeUpdate();
                }
            }

            // Închidem resursele
            workbook.close();
            inputStream.close();

            Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Logăm eroarea în logcat
            Log.e("Excel Processing Error", "Error processing Excel file", e);
            Toast.makeText(this, "Datele au fost încărcate cu succes!", Toast.LENGTH_LONG).show();
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
            case FORMULA:
                return cell.getCellFormula();
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

    private int getFacultateId(String numeFacultate, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDFacultate FROM Facultate WHERE Denumire = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, numeFacultate);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDFacultate");
            }
        } catch (Exception e) {
            Log.e("Get Facultate ID Error", "Error getting Facultate ID", e);
        }
        return id;
    }

    private int getDepartamentId(String numeDepartament, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDDepartament FROM Departament WHERE NumeDepartament = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, numeDepartament);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDDepartament");
            }
        } catch (Exception e) {
            Log.e("Get Departament ID Error", "Error getting Departament ID", e);
        }
        return id;
    }


    private int getSpecializareId(String numeSpecializare, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDSpecializare FROM Specializare WHERE Nume = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, numeSpecializare);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDSpecializare");
            }
        } catch (Exception e) {
            Log.e("Get Specializare ID Error", "Error getting Specializare ID", e);
        }
        return id;
    }

    private int getGrupaId(String grupa, Connection conn) {
        int id = -1;
        try {
            String query = "SELECT IDGrupa FROM Grupa WHERE Nume = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, grupa);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("IDGrupa");
            }
        } catch (Exception e) {
            Log.e("Get Grupa ID Error", "Error getting Grupa ID", e);
        }
        return id;
    }

}

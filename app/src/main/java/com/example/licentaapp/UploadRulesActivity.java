package com.example.licentaapp;
import static com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils.getPath;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UploadRulesActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    private Spinner spinnerCategories; // Declara variabila spinnerCategories aici

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_rules);

        spinnerCategories = findViewById(R.id.spinner_categories); // Inițializează spinnerCategories aici

        Button buttonUpload = findViewById(R.id.button_upload);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = spinnerCategories.getSelectedItem().toString();
                openFileChooser(selectedCategory);
            }
        });
    }

    private void openFileChooser(String category) {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri pdfUri = data.getData();
            String selectedCategory = spinnerCategories.getSelectedItem().toString();
            String filePath = getFilePathFromUri(pdfUri);
            if (filePath != null) {
                File file = new File(filePath);
                uploadFile(selectedCategory, file);
            } else {
                Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex != -1) {
                    String fileName = cursor.getString(columnIndex);
                    File tempFile = new File(getCacheDir(), fileName);
                    filePath = tempFile.getAbsolutePath();

                    FileUtils.copyInputStreamToFile(getContentResolver().openInputStream(uri), tempFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return filePath;
    }


    private void uploadFile(String category, File file) {
        Connection conn = ConnectionClass.connect();
        if (conn == null) {
            Toast.makeText(this, "Connection to database failed", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "INSERT INTO Regulament (Nume, Pdf, DataIncarcare, Categoria) VALUES (?, ?, GETDATE(), ?)";
        try {
            FileInputStream fis = new FileInputStream(file);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, file.getName());
            pstmt.setBinaryStream(2, fis, (int) file.length());
            pstmt.setString(3, category); // Adăugăm categoria
            pstmt.executeUpdate();
            Toast.makeText(this, "Fișier încărcat cu succes!", Toast.LENGTH_SHORT).show();
            pstmt.close();
            fis.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Eroare la încărcarea fișierului", Toast.LENGTH_SHORT).show();
        } finally {
            ConnectionClass.closeConnection(conn);
        }
    }
}
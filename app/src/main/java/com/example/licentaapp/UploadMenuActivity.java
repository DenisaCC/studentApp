package com.example.licentaapp;

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

public class UploadMenuActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    private Spinner spinnerCantine;
    private Uri selectedPdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_menu);

        spinnerCantine = findViewById(R.id.spinner_cantine);

        Button chooseFileButton = findViewById(R.id.chooseFileButton);
        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        Button uploadToSSMSButton = findViewById(R.id.uploadToSSMSButton);
        uploadToSSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCantine = spinnerCantine.getSelectedItem().toString();
                String filePath = getFilePathFromUri(selectedPdfUri);
                if (filePath != null) {
                    File file = new File(filePath);
                    uploadMenu(selectedCantine, file);
                } else {
                    Toast.makeText(UploadMenuActivity.this, "Failed to get file path", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedPdfUri = data.getData();
            String selectedCantine = spinnerCantine.getSelectedItem().toString();
            String filePath = getFilePathFromUri(selectedPdfUri);
            if (filePath != null) {
                File file = new File(filePath);
                uploadMenu(selectedCantine, file);
            } else {
                Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadMenu(String selectedCantina, File file) {
        Connection conn = ConnectionClass.connect();
        if (conn == null) {
            Toast.makeText(this, "Connection to database failed", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "UPDATE Cantina SET MeniuPDF = ? WHERE NumeCantina = ?";
        try {
            FileInputStream fis = new FileInputStream(file);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setBinaryStream(1, fis, (int) file.length());
            pstmt.setString(2, selectedCantina);
            pstmt.executeUpdate();
            Toast.makeText(this, "Fișier încărcat cu succes!", Toast.LENGTH_SHORT).show();
            pstmt.close();
            fis.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Eroare la încărcarea fișierului!", Toast.LENGTH_SHORT).show();
        } finally {
            ConnectionClass.closeConnection(conn);
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
                    // Adăugăm un mesaj de debug pentru a afișa calea către fișierul PDF
                    Log.d("FilePathDebug", "Calea către fișierul PDF este: " + filePath);

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

}

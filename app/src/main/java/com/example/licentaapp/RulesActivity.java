package com.example.licentaapp;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.Regulament;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesActivity extends AppCompatActivity {

    private Map<String, List<Regulament>> categoriiRegulamente = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        Log.d("RulesActivity", "Încărcarea regulamentului...");

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RulesActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });
        loadRegulamente();
    }

    private void loadRegulamente() {
        String query = "SELECT Nume, Pdf, Categoria FROM Regulament";

        try {
            Connection conn = ConnectionClass.connect();
            if (conn != null) {
                Log.d("RulesActivity", "Conexiunea la baza de date este stabilită.");

                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet resultSet = pstmt.executeQuery();

                while (resultSet.next()) {
                    String numeRegulament = resultSet.getString("Nume");
                    byte[] pdfData = resultSet.getBytes("Pdf");
                    String categoria = resultSet.getString("Categoria");

                    List<Regulament> regulamente = categoriiRegulamente.getOrDefault(categoria, new ArrayList<>());
                    regulamente.add(new Regulament(numeRegulament, pdfData));
                    categoriiRegulamente.put(categoria, regulamente);
                }

                conn.close();
                displayRegulamente();
            } else {
                Log.e("RulesActivity", "Conexiunea la baza de date a eșuat.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("RulesActivity", "Eroare la executarea interogării: " + e.getMessage());
        }

        Log.d("RulesActivity", "Am terminat încărcarea regulamentului.");
    }


    private void displayRegulamente() {
        ListView listView = findViewById(R.id.list_regulamente);
        List<String> categorii = new ArrayList<>(categoriiRegulamente.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categorii);
        listView.setAdapter(adapter);

        // Adaugă un listener pentru selectarea unei categorii
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String categorie = categorii.get(position);
                List<Regulament> regulamente = categoriiRegulamente.get(categorie);
                if (regulamente != null) {
                    showRegulamenteDialog(regulamente);
                }
            }
        });
    }

    private void showRegulamenteDialog(List<Regulament> regulamente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Descarcă");

        // Extrage numele regulamentelor pentru afișare în dialog box
        List<String> numeRegulamente = new ArrayList<>();
        for (Regulament regulament : regulamente) {
            numeRegulamente.add(regulament.getNume());
        }

        // Afișează numele regulamentelor într-o listă în dialog box
        builder.setItems(numeRegulamente.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // La clic pe un regulament, descarcă PDF-ul
                downloadPDF(regulamente.get(which));
            }
        });

        builder.setNegativeButton("Anulează", null);
        builder.show();
    }
    private void downloadPDF(Regulament regulament) {
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(regulament);
    }

    private class DownloadTask extends AsyncTask<Regulament, Void, Void> {
        @Override
        protected Void doInBackground(Regulament... regulamente) {
            Regulament regulament = regulamente[0];
            String fileName = regulament.getNume() + ".pdf";
            byte[] pdfData = regulament.getPdfData();

            try {
                FileOutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                output.write(pdfData);
                output.close();
                Log.d("DownloadTask", "Fișierul PDF a fost descărcat și salvat cu succes: " + fileName);

                sendNotification("Fișierul PDF a fost descărcat", "Fișierul " + fileName + " a fost salvat cu succes în directorul de descărcări.");

            } catch (IOException e) {
                Log.e("DownloadTask", "Eroare la salvarea fișierului PDF: " + e.getMessage());
            }

            return null;
        }
    }

        private void sendNotification(String title, String message) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.baseline_file_download_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(0, builder.build());
        }
    }
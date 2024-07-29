package com.example.licentaapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.NewsAdapter;
import com.example.licentaapp.Utils.NewsModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements NewsAdapter.OnDownloadClickListener {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsModel> announcements;
    private String username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        username = getIntent().getStringExtra("USERNAME");
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcements = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, announcements, this); // Setăm activitatea ca ascultător pentru descărcare
        recyclerView.setAdapter(newsAdapter);

        ImageButton backButton = findViewById(R.id.backBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        // Verificăm dacă username-ul este valid și obținem anunțurile corespunzătoare
        if (username != null) {
            int nrMatricol = getNrMatricolByUsername(username);
            if (nrMatricol != -1) {
                populateNews(nrMatricol);
            } else {
                Toast.makeText(this, "Utilizatorul nu are NrMatricol asociat", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Username-ul utilizatorului nu a fost găsit", Toast.LENGTH_SHORT).show();
        }
    }

    // Obținem NrMatricol după username
    private int getNrMatricolByUsername(String username) {
        int nrMatricol = -1;
        Connection con = ConnectionClass.connect();

        if (con != null) {
            String query = "SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nrMatricol = rs.getInt("NrMatricol");
                }
            } catch (SQLException e) {
                Log.e("NewsActivity", "Eroare la obținerea NrMatricol după username: " + e.getMessage());
            } finally {
                ConnectionClass.closeConnection(con);
            }
        } else {
            Log.e("NewsActivity", "Conexiunea la bază de date a eșuat");
        }

        return nrMatricol;
    }

    // Populăm anunțurile în RecyclerView
    private void populateNews(int nrMatricol) {
        Connection con = ConnectionClass.connect();

        if (con != null) {
            String query = "SELECT IDAnunt, Titlu, Data, Descriere, PDF FROM Anunt WHERE IDSpecializare IN " +
                    "(SELECT IDSpecializare FROM Student WHERE NrMatricol = ?)";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, nrMatricol);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("IDAnunt");
                    String title = rs.getString("Titlu");
                    String date = rs.getString("Data");
                    String description = rs.getString("Descriere");
                    byte[] pdfData = rs.getBytes("PDF");

                    NewsModel newsModel = new NewsModel(id, title, date, description, pdfData);
                    announcements.add(newsModel);
                }
                newsAdapter.notifyDataSetChanged();
            } catch (SQLException e) {
                Log.e("NewsActivity", "Eroare la popularea anunțurilor: " + e.getMessage());
            } finally {
                ConnectionClass.closeConnection(con);
            }
        } else {
            Log.e("NewsActivity", "Conexiunea la bază de date a eșuat");
        }
    }

    // Implementăm metoda din OnDownloadClickListener
    @Override
    public void onDownloadClick(int position) {
        NewsModel newsModel = announcements.get(position);

        // Verificăm dacă anunțul are fișier PDF
        if (newsModel.getPdfData() != null && newsModel.getPdfData().length > 0) {
            // Startăm task-ul de descărcare
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(newsModel);
        } else {
            Toast.makeText(this, "Acest anunț nu are fișier PDF atașat", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask pentru descărcare
    private class DownloadTask extends AsyncTask<NewsModel, Void, Void> {
        @Override
        protected Void doInBackground(NewsModel... newsModels) {
            NewsModel newsModel = newsModels[0];
            String fileName = "announcement_" + newsModel.getId() + ".pdf";
            byte[] pdfData = newsModel.getPdfData();

            FileOutputStream fos = null;
            try {
                fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(pdfData);
                Log.d("DownloadTask", "Fișierul PDF a fost descărcat și salvat cu succes: " + fileName);

                // Afișăm un Toast pentru a anunța utilizatorul
                runOnUiThread(() -> Toast.makeText(NewsActivity.this, "Fișierul PDF a fost descărcat și salvat", Toast.LENGTH_SHORT).show());

                // Trimitem o notificare către utilizator
                sendNotification("Fișier PDF descărcat", "Fișierul " + fileName + " a fost salvat în stocarea internă.");

            } catch (IOException e) {
                Log.e("DownloadTask", "Eroare la salvarea fișierului PDF: " + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }

    // Metodă pentru a trimite o notificare
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.baseline_file_download_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}

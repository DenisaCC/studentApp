package com.example.licentaapp;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.licentaapp.Connection.ConnectionClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CanteenActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "canteen_download_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen);

        createNotificationChannel();

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button canteen1Button = findViewById(R.id.canteen1Button);
        Button canteen2Button = findViewById(R.id.canteen2Button);

        canteen1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadDialog("Cantina 1");
            }
        });

        canteen2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadDialog("Cantina 2");
            }
        });
    }

    private void showDownloadDialog(final String canteenName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Descărcare Meniu");
        builder.setMessage("Doriți să descărcați meniul în format PDF?");
        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DownloadMenuTask(canteenName).execute();
            }
        });
        builder.setNegativeButton("Nu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private class DownloadMenuTask extends AsyncTask<Void, Void, File> {
        private String canteenName;

        DownloadMenuTask(String canteenName) {
            this.canteenName = canteenName;
        }

        @Override
        protected File doInBackground(Void... voids) {
            Connection conn = ConnectionClass.connect();
            if (conn == null) {
                return null;
            }

            File pdfFile = null;
            String query = "SELECT MeniuPDF FROM Cantina WHERE NumeCantina = ?";
            try {
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, canteenName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    InputStream inputStream = rs.getBinaryStream("MeniuPDF");
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    pdfFile = new File(downloadsDir, canteenName + "_menu.pdf");
                    FileOutputStream outputStream = new FileOutputStream(pdfFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                }
                rs.close();
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ConnectionClass.closeConnection(conn);
            }
            return pdfFile;
        }

        @Override
        protected void onPostExecute(File pdfFile) {
            if (pdfFile != null && pdfFile.exists()) {
                sendDownloadNotification(pdfFile);
                Toast.makeText(CanteenActivity.this, "Fișierul a fost descărcat", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CanteenActivity.this, "Descărcarea fișierului a eșuat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download Channel";
            String description = "Channel for download notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendDownloadNotification(@NonNull File file) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_file_download_24)
                .setContentTitle("Descărcare completă")
                .setContentText("Fișierul " + file.getName() + " a fost descărcat.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}

package com.example.licentaapp;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.CaminAdapter;
import com.example.licentaapp.Utils.CaminModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CaminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CaminAdapter caminAdapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camin);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        caminAdapter = new CaminAdapter(new ArrayList<>()); // Inițializează adaptorul cu o listă goală
        recyclerView.setAdapter(caminAdapter);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Obține username-ul utilizatorului curent din intent
        username = getIntent().getStringExtra("USERNAME");

        // Obține ID-ul facultății asociat cu studentul curent
        int idFacultate = getIdFacultateByUsername(username);
        if (idFacultate != -1) {
            // Obține informațiile despre caminele asociate cu facultatea respectivă
            List<CaminModel> camine = getCamineByFacultate(idFacultate);
            // Adaugă caminele în adaptorul RecyclerView
            caminAdapter.setCamine(camine);
        } else {
            Toast.makeText(this, "Utilizatorul nu a fost găsit în baza de date", Toast.LENGTH_SHORT).show();
        }
    }

    private int getIdFacultateByUsername(String username) {
        Log.d("CaminActivity", "Username: " + username); // Adaugă un mesaj de înregistrare pentru a afișa username-ul
        Connection con = ConnectionClass.connect();
        if (con != null) {
            String query = "SELECT IDFacultate FROM Student WHERE NrMatricol = (SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = ?)";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("IDFacultate");
                    } else {
                        return -1; // Utilizatorul nu a fost găsit
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
        return -1;
    }


    private List<CaminModel> getCamineByFacultate(int idFacultate) {
        List<CaminModel> camine = new ArrayList<>();
        Connection con = ConnectionClass.connect();
        if (con != null) {
            String query = "SELECT IDCamin, NumeCamin, Adresa, Administrator, Email, NrTelefon, Capacitate FROM Camin WHERE IDFacultate = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, idFacultate);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int idCamin = rs.getInt("IDCamin");
                        String numeCamin = rs.getString("NumeCamin");
                        String adresa = rs.getString("Adresa");
                        String administrator = rs.getString("Administrator");
                        String email = rs.getString("Email");
                        String nrTelefon = rs.getString("NrTelefon");
                        int capacitate = rs.getInt("Capacitate");
                        CaminModel camin = new CaminModel(idCamin, numeCamin, adresa, administrator, email, nrTelefon, capacitate);
                        camine.add(camin);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
        return camine;
    }

}

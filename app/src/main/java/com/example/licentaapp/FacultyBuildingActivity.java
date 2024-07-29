package com.example.licentaapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.BuildingFacultyAdapter;
import com.example.licentaapp.Utils.BuildingFacultyModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyBuildingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BuildingFacultyAdapter corpFacultateAdapter;
    private String username;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_building);

        recyclerView = findViewById(R.id.recyclerViewCorpFacultate);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        corpFacultateAdapter = new BuildingFacultyAdapter(new ArrayList<>());
        recyclerView.setAdapter(corpFacultateAdapter);

        // Obține username-ul utilizatorului curent din intent
        username = getIntent().getStringExtra("USERNAME");

        // Obține ID-ul facultății asociat cu studentul curent
        int idFacultate = getIdFacultateByUsername(username);
        if (idFacultate != -1) {
            // Obține informațiile despre corpurile facultății asociate cu facultatea respectivă
            List<BuildingFacultyModel> corpuriFacultate = getCorpuriFacultateByFacultate(idFacultate);
            // Adaugă corpurile facultății în adaptorul RecyclerView
            corpFacultateAdapter.setCorpuriFacultate(corpuriFacultate);
        } else {
            Toast.makeText(this, "Utilizatorul nu a fost găsit în baza de date", Toast.LENGTH_SHORT).show();
        }
    }

    private int getIdFacultateByUsername(String username) {
        Log.d("FacultyBuildingActivity", "Username: " + username);
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

    private List<BuildingFacultyModel> getCorpuriFacultateByFacultate(int idFacultate) {
        List<BuildingFacultyModel> corpuriFacultate = new ArrayList<>();
        Connection con = ConnectionClass.connect();
        if (con != null) {
            String query = "SELECT IDCorpFacultate, NumeCorpFacultate, Adresa FROM CorpFacultate WHERE IDFacultate = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, idFacultate);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int idCorpFacultate = rs.getInt("IDCorpFacultate");
                        String numeCorpFacultate = rs.getString("NumeCorpFacultate");
                        String adresa = rs.getString("Adresa");
                        BuildingFacultyModel corpFacultate = new BuildingFacultyModel(idCorpFacultate, numeCorpFacultate, adresa);
                        corpuriFacultate.add(corpFacultate);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
        return corpuriFacultate;
    }
}

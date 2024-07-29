package com.example.licentaapp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.licentaapp.Connection.ConnectionClass;

import java.sql.Connection;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;
import java.sql.Statement;

public class TaxesActivity extends AppCompatActivity {

    // Declarațiile pentru TextView-urile din layout
    private TextView textViewCazareLunaCurenta;
    private TextView textViewCazareLunaAnterioara;
    private TextView textViewScholarSem1;
    private TextView textViewScholarSem2;
    private TextView textViewWarning;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxes);

        // Inițializarea TextView-urilor
        textViewCazareLunaCurenta = findViewById(R.id.textViewCazareLunaCurenta);
        textViewCazareLunaAnterioara = findViewById(R.id.textViewCazareLunaAnterioara);
        textViewScholarSem1 = findViewById(R.id.textViewTaxaSem1);
        textViewScholarSem2 = findViewById(R.id.textViewTaxaSem2);
        textViewWarning = findViewById(R.id.textViewHostingWarning);

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaxesActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("USERNAME");
            int nrMatricol = getNrMatricolForUsername(username);
            if (nrMatricol != -1) {
                // Apelarea metodei pentru a prelua și afișa datele despre taxe
                showTaxesData(nrMatricol);
            } else {
                Toast.makeText(this, "Nu s-a putut găsi numărul matricol pentru utilizatorul " + username, Toast.LENGTH_SHORT).show();
            }
        }

        // Inițializarea cardView-urilor și adăugarea gestionării clicurilor
        CardView cardViewHostingTaxes = findViewById(R.id.cardViewHostingTaxes);
        CardView cardViewScholarTaxes = findViewById(R.id.cardViewScholarTaxes);

        // Ascunde TextView-urile inițial
        textViewCazareLunaCurenta.setVisibility(View.GONE);
        textViewCazareLunaAnterioara.setVisibility(View.GONE);
        textViewScholarSem1.setVisibility(View.GONE);
        textViewScholarSem2.setVisibility(View.GONE);

        // Setează un click listener pentru cardul de taxe de cazare
        cardViewHostingTaxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Afișează sau ascunde taxele de cazare în funcție de vizibilitatea lor actuală
                if (textViewCazareLunaCurenta.getVisibility() == View.VISIBLE) {
                    textViewCazareLunaCurenta.setVisibility(View.GONE);
                    textViewCazareLunaAnterioara.setVisibility(View.GONE);
                } else {
                    // Dacă nu sunt vizibile, le afișăm
                    int nrMatricol = getNrMatricolForUsername(username);
                    if (nrMatricol != -1) {
                        // Apelăm metoda pentru a prelua și afișa datele despre taxe pentru numărul matricol dat
                        showTaxesData(nrMatricol);
                        textViewCazareLunaCurenta.setVisibility(View.VISIBLE);
                        textViewCazareLunaAnterioara.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(TaxesActivity.this, "Nu s-a putut găsi numărul matricol pentru utilizatorul " + username, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Setează un click listener pentru cardul de taxe de școlarizare
        cardViewScholarTaxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Afișează sau ascunde taxele de școlarizare în funcție de vizibilitatea lor actuală
                if (textViewScholarSem1.getVisibility() == View.VISIBLE) {
                    textViewScholarSem1.setVisibility(View.GONE);
                    textViewScholarSem2.setVisibility(View.GONE);
                } else {
                    // Dacă nu sunt vizibile, le afișăm
                    int nrMatricolScholar = getNrMatricolForUsername(username);
                    if (nrMatricolScholar != -1) {
                        // Apelăm metoda pentru a prelua și afișa datele despre taxe pentru numărul matricol dat
                        showTaxesData(nrMatricolScholar);
                        textViewScholarSem1.setVisibility(View.VISIBLE);
                        textViewScholarSem2.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(TaxesActivity.this, "Nu s-a putut găsi numărul matricol pentru utilizatorul " + username, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Metodă pentru preluarea și afișarea datelor despre taxe
    // Metodă pentru preluarea și afișarea datelor despre taxe
    private void showTaxesData(int nrMatricol) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Se stabilește conexiunea la baza de date
            conn = ConnectionClass.connect();

            // Se definește interogarea SQL pentru a prelua datele despre taxe
            String query = "SELECT TaxaCazareLunaInCurs, TaxaCazareLunaAnterioara, TaxaScolarizareSemestrul1, TaxaScolarizareSemestrul2 FROM TaxeStudenti WHERE NrMatricol = ?";

            // Se pregătește interogarea pentru execuție
            statement = conn.prepareStatement(query);

            // Setarea valorii parametrului pentru NrMatricol
            statement.setInt(1, nrMatricol);

            // Se execută interogarea și se obține rezultatul
            resultSet = statement.executeQuery();

            // Se verifică dacă există rezultate
            if (resultSet.next()) {

                // Obține valorile ca șiruri de caractere
                String taxaCazareLunaCurentaStr = resultSet.getString("TaxaCazareLunaInCurs");
                String taxaCazareLunaAnterioaraStr = resultSet.getString("TaxaCazareLunaAnterioara");
                String taxaScolarizareSem1Str = resultSet.getString("TaxaScolarizareSemestrul1");
                String taxaScolarizareSem2Str = resultSet.getString("TaxaScolarizareSemestrul2");

// Elimină virgula din fiecare șir de caractere, dacă există
                taxaCazareLunaCurentaStr = taxaCazareLunaCurentaStr.replace(",", "");
                taxaCazareLunaAnterioaraStr = taxaCazareLunaAnterioaraStr.replace(",", "");
                taxaScolarizareSem1Str = taxaScolarizareSem1Str.replace(",", "");
                taxaScolarizareSem2Str = taxaScolarizareSem2Str.replace(",", "");

// Convertă șirurile în valori numerice
                int taxaCazareLunaCurenta = Integer.parseInt(taxaCazareLunaCurentaStr);
                int taxaCazareLunaAnterioara = Integer.parseInt(taxaCazareLunaAnterioaraStr);
                int taxaScolarizareSem1 = Integer.parseInt(taxaScolarizareSem1Str);
                int taxaScolarizareSem2 = Integer.parseInt(taxaScolarizareSem2Str);


                // Verificăm taxele pentru a determina mesajele corespunzătoare
                if (taxaCazareLunaCurenta == 0) {
                    textViewCazareLunaCurenta.setText("Nu aveți taxe de cazare de plată!");
                } else {
                    if (taxaCazareLunaAnterioara != 0) {
                        textViewCazareLunaCurenta.setText("Taxa de plată luna anterioară: " + String.valueOf((taxaCazareLunaAnterioara)) + " lei" + "\n" + "Taxa de plată luna curentă: " + String.valueOf((taxaCazareLunaCurenta)) + " lei");
                    } else {
                        textViewCazareLunaCurenta.setText("Total de plată: " + String.valueOf((taxaCazareLunaCurenta)) + " lei");
                    }
                }
                if (taxaScolarizareSem2 == 0) {
                    textViewScholarSem2.setText("Nu aveți taxe de școlarizare de plată!");
                } else {
                    textViewScholarSem2.setText("Semestrul 1: " + String.valueOf((taxaScolarizareSem1)) + " lei" + "\n" + "Semestrul 2: " + String.valueOf(taxaScolarizareSem2) + " lei");
                }
                if (taxaCazareLunaAnterioara != 0 ) {
                    textViewWarning.setVisibility(View.VISIBLE);
                    // Calculăm suma celor două taxe
                    int totalDePlata = taxaCazareLunaAnterioara + taxaCazareLunaCurenta;
                    textViewWarning.setText("Avertisment: Aveți taxe restante de plată. Total de plată: " + totalDePlata + " lei");

                }
            } else {
                Toast.makeText(this, "Nu s-au găsit taxe pentru numărul matricol " + nrMatricol, Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Se închid resursele
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            ConnectionClass.closeConnection(conn);
        }
    }

    private int getNrMatricolForUsername(String username) {
        Connection con = ConnectionClass.connect();
        if (con != null) {
            try {
                Statement stmt = con.createStatement();
                String query = "SELECT NrMatricol FROM Utilizator WHERE NumeUtilizator = '" + username + "'";
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int nrMatricol = rs.getInt("NrMatricol");
                    return nrMatricol;
                } else {
                    return -1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            } finally {
                ConnectionClass.closeConnection(con);
            }
        }
        return -1;
    }

}
package com.example.licentaapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.Specialisation;
import com.example.licentaapp.Utils.SpecialisationAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SpecialisationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SpecialisationAdapter adapter;
    private int departamentID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialisation);

        String departmentName = "Departamentul de " + getIntent().getStringExtra("departmentName");
        int departmentID = getIntent().getIntExtra("departmentID", -1);

        TextView selectedDepartmentTextView = findViewById(R.id.departmentName);
        selectedDepartmentTextView.setText(departmentName);

        recyclerView = findViewById(R.id.recyclerView_specialisation);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        departamentID = getIntent().getIntExtra("departmentID", -1);


        // Initializează adaptorul pentru RecyclerView
        adapter = new SpecialisationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Obține și afișează specializările corespunzătoare departamentului
        new GetSpecialisationDataTask().execute(departmentID);

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpecialisationActivity.this, FacultyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("BACK_PRESSED", true);
                startActivity(intent);
            }
        });
    }

    private class GetSpecialisationDataTask extends AsyncTask<Integer, Void, List<Specialisation>> {

    @Override
        protected List<Specialisation> doInBackground(Integer... integers) {
            int departmentID = integers[0];
            List<Specialisation> specialisations = new ArrayList<>();
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                statement = connection.createStatement();
                String specialisationQuery = "SELECT Nume, Descriere FROM Specializare WHERE IDDepartament = " + departmentID;
                resultSet = statement.executeQuery(specialisationQuery);

                while (resultSet.next()) {
                    String nume = resultSet.getString("Nume");
                    String descriere = resultSet.getString("Descriere");
                    Specialisation specializare = new Specialisation(nume, descriere);
                    specialisations.add(specializare);
                }

                Log.d(TAG, "Specializations: " + specialisations.toString());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return specialisations;

        }

    @Override
    protected void onPostExecute(List<Specialisation> specialisations) {
        ImageView imageView = findViewById(R.id.imageView);
        int imageResourceId = R.drawable.loading;
        switch (departamentID) {
            case 1:
                imageResourceId = R.drawable.biodepafab;
                break;
            case 2:
                imageResourceId = R.drawable.biodepbbm;
                break;
            // Alte cazuri
        }
        imageView.setImageResource(imageResourceId);
        // Actualizează adaptorul RecyclerView cu specializările obținute din baza de date
        adapter.setSpecialisations(specialisations);
        adapter.notifyDataSetChanged();
    }
}
}

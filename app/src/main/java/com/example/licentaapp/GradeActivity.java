package com.example.licentaapp;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.Grade;
import com.example.licentaapp.Utils.GradeAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GradeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGrade;
    private GradeAdapter gradeAdapter;
    private List<Grade> gradeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);

        int nrMatricol = getIntent().getIntExtra("NR_MATRICOL", 0); // 0 este valoarea implicită în cazul în care nu se găsește niciun număr matricol în intent
        Log.d("GradeActivity", "Numărul matricolului: " + nrMatricol);
        recyclerViewGrade = findViewById(R.id.recyclerViewGrade);
        recyclerViewGrade.setLayoutManager(new LinearLayoutManager(this));

        gradeList = new ArrayList<>();
        gradeAdapter = new GradeAdapter(gradeList);
        recyclerViewGrade.setAdapter(gradeAdapter);

        // Inițializează GetGradesTask cu valoarea nrMatricol
        new GetGradesTask(nrMatricol).execute();
    }


    private class GetGradesTask extends AsyncTask<Void, Void, List<Grade>> {
        private int nrMatricol;

        public GetGradesTask(int nrMatricol) {
            this.nrMatricol = nrMatricol;
        }

        @Override
        protected List<Grade> doInBackground(Void... voids) {
            List<Grade> grades = new ArrayList<>();
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);

                statement = connection.createStatement();
                String gradeQuery = "SELECT Nota, DataNota, Disciplina.Nume AS NumeDisciplina, Profesor.Nume AS NumeProfesor, Profesor.Prenume AS PrenumeProfesor " +
                        "FROM Nota " +
                        "INNER JOIN Disciplina ON Nota.IDDisciplina = Disciplina.IDDisciplina " +
                        "INNER JOIN Profesor ON Nota.IDProfesor = Profesor.IDProfesor " +
                        "WHERE NrMatricol = '" + nrMatricol + "'";

                resultSet = statement.executeQuery(gradeQuery);

                while (resultSet.next()) {
                    String numeDisciplina = resultSet.getString("NumeDisciplina");
                    String numeProfesor = resultSet.getString("NumeProfesor") + " " + resultSet.getString("PrenumeProfesor");
                    float nota = resultSet.getFloat("Nota");
                    String dataNota = resultSet.getString("DataNota");

                    Grade grade = new Grade(numeDisciplina, nota, numeProfesor, dataNota);
                    grades.add(grade);
                }

            } catch (Exception e) {
                Log.e("GradeActivity", "Error fetching grades: " + e.getMessage());
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return grades;
        }

        @Override
        protected void onPostExecute(List<Grade> grades) {
            if (grades != null) {
                gradeList.addAll(grades);
                gradeAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(GradeActivity.this, "Failed to fetch grades", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

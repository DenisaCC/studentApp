package com.example.licentaapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Connection.ConnectionClass;
import com.example.licentaapp.Utils.LetterImageView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvenWeekActivity extends AppCompatActivity {

    private ListView listView;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_even_week);

        // Inițializează ListView-ul pentru afișarea orarului
        listView = findViewById(R.id.lvSchedule);
        LinearLayout scheduleDetailsLayout = findViewById(R.id.scheduleDetailsLayout);
        String[] daysOfWeek = getResources().getStringArray(R.array.Week);
        adapter = new ScheduleAdapter(this, daysOfWeek, new HashMap<String, List<String>>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Actualizează isFirstClick pentru poziția selectată în adapter
                adapter.isFirstClickList.set(position, true);

                // Actualizează poziția selectată în adapter
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
                // Afișează cardul de detalii pentru ziua selectată
                adapter.showDetailCardView(position, view); // Transmit convertView (view) către showDetailCardView
            }
        });

        // Încarcă datele orarului pentru utilizatorul curent
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("USERNAME");
            if (username != null) {
                loadScheduleData(username);
            } else {
                // Gestionați cazul în care nu a fost furnizat niciun 'username'
            }
        } else {
            // Gestionați cazul în care intentul este null
        }

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadScheduleData(String username) {
        new LoadScheduleDataTask(username).execute();
    }

    private class LoadScheduleDataTask extends AsyncTask<Void, Void, Map<String, List<String>>> {
        private String mUsername;

        public LoadScheduleDataTask(String username) {
            mUsername = username;
        }

        @Override
        protected Map<String, List<String>> doInBackground(Void... voids) {
            Map<String, List<String>> scheduleData = new HashMap<>();
            Connection connection = null;
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);
                // Extrageți ID-ul grupului utilizatorului
                Statement groupStatement = connection.createStatement();
                String groupQuery = "SELECT S.IDGrupa FROM Utilizator U JOIN Student S ON U.NrMatricol = S.NrMatricol WHERE U.NumeUtilizator = '" + mUsername + "'";
                System.out.println("Query: " + groupQuery);
                ResultSet groupResultSet = groupStatement.executeQuery(groupQuery);
                int userGroupID = -1; // Va stoca ID-ul grupului utilizatorului
                if (groupResultSet.next()) {
                    userGroupID = groupResultSet.getInt("IDGrupa");
                    System.out.println("User group ID: " + userGroupID); // Afișați ID-ul grupului pentru a verifica dacă este corect
                } else {
                    System.out.println("Nu s-au găsit rezultate pentru utilizatorul: " + mUsername);
                }
                groupResultSet.close();
                groupStatement.close();

                if (userGroupID != -1) { // Verificați dacă s-a găsit un ID de grup valid pentru utilizator
                    Resources res = getResources();
                    String[] daysOfWeek = res.getStringArray(R.array.Week);

                    for (String day : daysOfWeek) {
                        Statement statement = connection.createStatement();
                        String query = "SELECT D.Nume AS NumeDisciplina, P.Nume AS NumeProfesor, P.Prenume AS PrenumeProfesor, O.Sala AS Sala, O.OraInceput AS oraInceput, O.OraSfarsit AS oraSfarsit, O.TipActivitate AS TipActivitate" +
                                " FROM Orar O " +
                                " JOIN Disciplina D ON O.IDDisciplina = D.IDDisciplina " +
                                " JOIN Profesor P ON D.IDProfesor = P.IDProfesor " +
                                " WHERE O.ZiSaptamana = '" + day + "' AND O.IDGrupa = " + userGroupID + " AND O.paritateSaptamana = 'Para'";
                        System.out.println("Interogare pentru ziua " + day + ": " + query); // Instrucțiune de imprimare pentru interogare
                        ResultSet resultSet = statement.executeQuery(query);
                        System.out.println("Query: " + query);

                        List<String> scheduleForCurrentDay = new ArrayList<>();
                        while (resultSet.next()) {
                            String discipline = resultSet.getString("NumeDisciplina");
                            String numeProfesor = resultSet.getString("NumeProfesor");
                            String prenumeProfesor = resultSet.getString("PrenumeProfesor");
                            String sala = resultSet.getString("Sala");
                            String oraInceput = resultSet.getString("OraInceput").substring(0, 5);
                            String oraSfarsit = resultSet.getString("OraSfarsit").substring(0, 5);
                            String intervalOrar = oraInceput + " - " + oraSfarsit;
                            String tipActivitate = resultSet.getString("TipActivitate");
                            String scheduleEntry = discipline + " - " + numeProfesor + " " + prenumeProfesor + " - " + sala + " - " + intervalOrar + " - " + tipActivitate;
                            scheduleForCurrentDay.add(scheduleEntry);
                        }
                        scheduleData.put(day, scheduleForCurrentDay);
                        resultSet.close();
                        statement.close();
                        System.out.println("Evenimente pentru ziua " + day + ": " + scheduleForCurrentDay);
                    }
                } else {
                    // Gestionați cazul în care nu s-a găsit un ID de grup valid pentru utilizator
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return scheduleData;
        }

        @Override
        protected void onPostExecute(Map<String, List<String>> scheduleData) {
            if (scheduleData != null && !scheduleData.isEmpty()) {
                adapter.setScheduleData(scheduleData);
                adapter.notifyDataSetChanged();
            } else {
                // Gestionați cazul în care nu s-au încărcat datele orarului
            }
        }
    }

    public class ScheduleAdapter extends ArrayAdapter<String> {
        private final Context mContext;
        private final List<String> mDaysOfWeek;
        private Map<String, List<String>> mScheduleData;

        public void clearScheduleData() {
            mScheduleData.clear();
        }

        private int mSelectedPosition = -1;
        private List<Boolean> isFirstClickList;

        public ScheduleAdapter(Context context, String[] daysOfWeek, Map<String, List<String>> scheduleData) {
            super(context, 0, daysOfWeek);
            mContext = context;
            mDaysOfWeek = Arrays.asList(daysOfWeek);
            mScheduleData = scheduleData;
            isFirstClickList = new ArrayList<>();
            for (int i = 0; i < daysOfWeek.length; i++) {
                isFirstClickList.add(true);
            }
        }

        public void setSelectedPosition(int position) {
            mSelectedPosition = position;
        }

        public Map<String, List<String>> getScheduleData() {
            return mScheduleData;
        }

        public void setScheduleData(Map<String, List<String>> scheduleData) {
            mScheduleData = scheduleData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String dayOfWeek = mDaysOfWeek.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_schedule_single_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.ivSchedule.setText(dayOfWeek.substring(0, 1));

            if (position == mSelectedPosition) {
                viewHolder.scheduleDetailsLayout.setVisibility(View.VISIBLE);
                viewHolder.tvNoCourses.setVisibility(isFirstClickList.get(position) ? View.VISIBLE : View.GONE);
            } else {
                viewHolder.scheduleDetailsLayout.setVisibility(View.GONE);
                viewHolder.tvSchedule.setText(dayOfWeek);
            }
            return convertView;
        }

        private void showDetailCardView(int position, View convertView) {
            String selectedDay = mDaysOfWeek.get(position);
            LinearLayout scheduleDetailsLayout = convertView.findViewById(R.id.scheduleDetailsLayout);
            TextView tvNoCourses = convertView.findViewById(R.id.tvNoCourses);

            // Verificați dacă există date pentru ziua selectată
            if (mScheduleData.containsKey(selectedDay)) {
                List<String> eventsForSelectedDay = mScheduleData.get(selectedDay);
                scheduleDetailsLayout.removeAllViews();

                // Verificați dacă există evenimente pentru ziua selectată
                if (!eventsForSelectedDay.isEmpty()) {
                    for (String event : eventsForSelectedDay) {
                        View eventDetailView = LayoutInflater.from(mContext).inflate(R.layout.course_details, null);
                        TextView tvSubjectName = eventDetailView.findViewById(R.id.tvSubjectName);
                        TextView tvProfessor = eventDetailView.findViewById(R.id.tvProfessor);
                        TextView tvRoom = eventDetailView.findViewById(R.id.tvRoom);
                        TextView tvTime = eventDetailView.findViewById(R.id.tvTime);
                        TextView tvActivity = eventDetailView.findViewById(R.id.tvActivityName);

                        String[] eventDetails = event.split(" - ");
                        String subjectName = eventDetails[0];
                        String professorName = eventDetails[1];
                        String room = eventDetails[2];
                        String timeInterval = eventDetails[3] + " - " + eventDetails[4];
                        String activity = eventDetails[5];

                        tvSubjectName.setText(subjectName);
                        tvProfessor.setText("Profesor: " + professorName);
                        tvRoom.setText(room);
                        tvTime.setText("Ora: " + timeInterval);
                        tvActivity.setText(activity);

                        scheduleDetailsLayout.addView(eventDetailView);
                    }
                    tvNoCourses.setVisibility(View.GONE);
                } else {
                    // Actualizează starea isFirstClick pentru elementul selectat
                    isFirstClickList.set(position, true);
                    tvNoCourses.setVisibility(isFirstClickList.get(position) ? View.VISIBLE : View.GONE);
                }
            }
        }

        private class ViewHolder {
            LetterImageView ivSchedule;
            TextView tvSchedule;
            LinearLayout scheduleDetailsLayout;
            TextView tvNoCourses;

            ViewHolder(View convertView) {
                ivSchedule = convertView.findViewById(R.id.ivSchedule);
                tvSchedule = convertView.findViewById(R.id.tvSchedule);
                scheduleDetailsLayout = convertView.findViewById(R.id.scheduleDetailsLayout);
                tvNoCourses = convertView.findViewById(R.id.tvNoCourses);
            }
        }
    }
}

package com.example.licentaapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class ProfEvenWeekActivity extends AppCompatActivity {

    private ListView listView;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_even_week);

        // Inițializează ListView-ul pentru afișarea orarului
        listView = findViewById(R.id.lvSchedule);
        LinearLayout programDetailsLayout = findViewById(R.id.programDetailsLayout);
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

        ImageButton backButton = findViewById(R.id.buttonDrawerToggle);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Încarcă datele orarului pentru utilizatorul curent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        Log.d("USERNAME:", username);
        if (username != null) {
            loadScheduleData(getIdProfesorFromUsername(username));
        } else {
            // Gestionați cazul în care nu a fost furnizat niciun 'username'
        }

    }

    private void loadScheduleData(int idProf) {
        new LoadScheduleDataTask(idProf).execute();
    }

    private class LoadScheduleDataTask extends AsyncTask<Void, Void, Map<String, List<String>>> {
        private int idProf;

        public LoadScheduleDataTask(int idProf) {
            this.idProf = idProf;
        }

        @Override
        protected Map<String, List<String>> doInBackground(Void... voids) {
            Map<String, List<String>> scheduleData = new HashMap<>();
            Connection connection = null;
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                String url = "jdbc:jtds:sqlserver://" + ConnectionClass.ip + ":" + ConnectionClass.port + ";DatabaseName=" + ConnectionClass.db + ";user=" + ConnectionClass.un + ";password=" + ConnectionClass.pass + ";";
                connection = DriverManager.getConnection(url);
                Resources res = getResources();
                String[] daysOfWeek = res.getStringArray(R.array.Week);

                for (String day : daysOfWeek) {
                    Statement statement = connection.createStatement();
                    String query = "SELECT Materie, TipActivitate, OraInceput, OraSfarsit, Sala, Facultate, Departament, Specializare, AnDeStudiu, Grupa " +
                            "FROM ProgramProfesor " +
                            "WHERE ZiuaSaptamânii = '" + day + "' AND ParitateSaptamana = 'Pară' AND IDProfesor = " + idProf;

                    ResultSet resultSet = statement.executeQuery(query);

                    List<String> scheduleForCurrentDay = new ArrayList<>();
                    while (resultSet.next()) {
                        String materie = resultSet.getString("Materie");
                        String tipActivitate = resultSet.getString("TipActivitate");
                        String facultate = resultSet.getString("Facultate");
                        String departament = resultSet.getString("Departament");
                        String specializare = resultSet.getString("Specializare");
                        String sala = resultSet.getString("Sala");
                        String oraInceput = resultSet.getTime("OraInceput").toString().substring(0, 5);
                        String oraSfarsit = resultSet.getTime("OraSfarsit").toString().substring(0, 5);
                        String intervalOrar = oraInceput + " - " + oraSfarsit;
                        String scheduleEntry = materie + " - " + tipActivitate + " - " + facultate + " - " + departament + " - " + specializare + " - "+ sala + " - " + intervalOrar;
                        scheduleForCurrentDay.add(scheduleEntry);
                    }

                    scheduleData.put(day, scheduleForCurrentDay);
                    resultSet.close();
                    statement.close();
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_prof_program_single_item, parent, false);
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
            LinearLayout scheduleDetailsLayout = convertView.findViewById(R.id.programDetailsLayout);
            TextView tvNoCourses = convertView.findViewById(R.id.tvNoCourses);

            // Verificați dacă există date pentru ziua selectată
            if (mScheduleData.containsKey(selectedDay)) {
                List<String> eventsForSelectedDay = mScheduleData.get(selectedDay);
                scheduleDetailsLayout.removeAllViews();

                // Verificați dacă există evenimente pentru ziua selectată
                if (!eventsForSelectedDay.isEmpty()) {
                    for (String event : eventsForSelectedDay) {
                        View eventDetailView = LayoutInflater.from(mContext).inflate(R.layout.prof_item_details, null);
                        TextView tvSubjectName = eventDetailView.findViewById(R.id.tvSubjectName);
                        TextView tvTypeActivity = eventDetailView.findViewById(R.id.tvTypeActivity);
                        TextView tvFaculty = eventDetailView.findViewById(R.id.tvFaculty);
                        TextView tvDepartment = eventDetailView.findViewById(R.id.tvDepartment);
                        TextView tvSpecialisation = eventDetailView.findViewById(R.id.tvSpecialisation);
                        TextView tvRoom = eventDetailView.findViewById(R.id.tvRoom);
                        TextView tvTime = eventDetailView.findViewById(R.id.tvTime);

                        String[] eventDetails = event.split(" - ");
                        String subjectName = eventDetails[0];
                        String typeActivity = eventDetails[1];
                        String faculty = eventDetails[2];
                        String department = eventDetails[3];
                        String specialisation = eventDetails[4];
                        String room = eventDetails[5];
                        String timeInterval = eventDetails[6] + " - " + eventDetails[7];

                        tvSubjectName.setText(subjectName);
                        tvTypeActivity.setText(typeActivity);
                        tvFaculty.setText(faculty);
                        tvDepartment.setText("Departament: " + department);
                        tvSpecialisation.setText("Specializare: " + specialisation);
                        tvRoom.setText("Sala: " + room);
                        tvTime.setText("Ora: " + timeInterval);

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
                scheduleDetailsLayout = convertView.findViewById(R.id.programDetailsLayout);
                tvNoCourses = convertView.findViewById(R.id.tvNoCourses);
            }
        }
    }

    private int getIdProfesorFromUsername(String username) {
        int idProfesor = -1; // Initializati cu un ID invalid

        Connection connection = null;
        try {
            connection = ConnectionClass.connect();
            Statement statement = connection.createStatement();
            String query = "SELECT IDProfesor FROM UtilizatorProfesor WHERE NumeUtilizator = '" + username + "'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                idProfesor = resultSet.getInt("IDProfesor");
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionClass.closeConnection(connection); // Închideți conexiunea în blocul finally
        }
        return idProfesor;
    }
}



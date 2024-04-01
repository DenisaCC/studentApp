package com.example.licentaapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.licentaapp.Utils.LetterImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        ListView listView = findViewById(R.id.lvSchedule);

        String[] daysOfWeek = getResources().getStringArray(R.array.Week);

        // Datele programului pentru fiecare zi
        Map<String, List<String>> scheduleData = new HashMap<>();
        // Adaugă aici datele de program pentru fiecare zi

        ScheduleAdapter adapter = new ScheduleAdapter(this, daysOfWeek, scheduleData);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDay = daysOfWeek[position];
                List<String> coursesForSelectedDay = scheduleData.get(selectedDay);
                // Aici afișezi lista de cursuri pentru ziua selectată, de exemplu, folosind un AlertDialog
            }
        });
    }

    private static class ScheduleAdapter extends ArrayAdapter<String> {
        private final Context mContext;
        private final List<String> mDaysOfWeek;
        private final Map<String, List<String>> mScheduleData;

        public ScheduleAdapter(Context context, String[] daysOfWeek, Map<String, List<String>> scheduleData) {
            super(context, 0, daysOfWeek);
            mContext = context;
            mDaysOfWeek = Arrays.asList(daysOfWeek);
            mScheduleData = scheduleData;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String dayOfWeek = mDaysOfWeek.get(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_schedule_single_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.ivSchedule = convertView.findViewById(R.id.ivSchedule);
                viewHolder.tvSchedule = convertView.findViewById(R.id.tvSchedule);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.ivSchedule.setText(dayOfWeek.substring(0, 1));
            viewHolder.tvSchedule.setText(dayOfWeek);

            return convertView;
        }

        private static class ViewHolder {
            LetterImageView ivSchedule;
            TextView tvSchedule;
        }
    }
}

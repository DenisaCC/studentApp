package com.example.licentaapp.Utils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.util.List;
public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<Grade> gradeList;

    public GradeAdapter(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = gradeList.get(position);
        holder.textViewDisciplina.setText(grade.getDiscipline());
        holder.textViewNota.setText(String.valueOf(grade.getGrade()));

        // Ascunde detaliile suplimentare la început
        holder.textViewProfesor.setVisibility(View.GONE);
        holder.textViewData.setVisibility(View.GONE);

        // Adaugă un eveniment de clic pentru a afișa detaliile suplimentare
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.textViewProfesor.getVisibility() == View.VISIBLE) {
                    holder.textViewProfesor.setVisibility(View.GONE);
                    holder.textViewData.setVisibility(View.GONE);
                } else {
                    holder.textViewProfesor.setVisibility(View.VISIBLE);
                    holder.textViewData.setVisibility(View.VISIBLE);
                }
            }
        });

        // Atribuie valorile detaliilor suplimentare
        holder.textViewProfesor.setText("Profesor: " + grade.getProfessor());
        holder.textViewData.setText("Data: " + grade.getDate());
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    public static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDisciplina, textViewProfesor, textViewData, textViewNota;
        CardView cardView;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDisciplina = itemView.findViewById(R.id.textViewDisciplina);
            textViewProfesor = itemView.findViewById(R.id.textViewProfesor);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewNota = itemView.findViewById(R.id.textViewNota);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setGrades(List<Grade> gradeList) {
        this.gradeList = gradeList;
        notifyDataSetChanged(); // Notifică adaptorul că datele s-au schimbat
    }
}

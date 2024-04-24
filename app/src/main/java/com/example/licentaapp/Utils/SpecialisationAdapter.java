package com.example.licentaapp.Utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.util.List;
public class SpecialisationAdapter extends RecyclerView.Adapter<SpecialisationAdapter.ViewHolder> {

    private List<Specialisation> specialisations;

    public SpecialisationAdapter(List<Specialisation> specialisations) {
        this.specialisations = specialisations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_specialisation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Specialisation specialisation = specialisations.get(position);
        holder.bindData(specialisation);
    }

    @Override
    public int getItemCount() {
        return specialisations.size();
    }

    public void setSpecialisations(List<Specialisation> specialisations) {
        this.specialisations = specialisations;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private TextView specialisationNameTextView;
        private TextView additionalTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            specialisationNameTextView = itemView.findViewById(R.id.specialisationName);
            additionalTextView = itemView.findViewById(R.id.descriptionText);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (additionalTextView.getVisibility() == View.GONE) {
                        additionalTextView.setVisibility(View.VISIBLE);
                    } else {
                        additionalTextView.setVisibility(View.GONE);
                    }
                    cardView.requestLayout();
                }
            });
        }

        public void bindData(Specialisation specializare) {
            specialisationNameTextView.setText(specializare.getName());
            additionalTextView.setText(specializare.getDescription());
        }
    }
}

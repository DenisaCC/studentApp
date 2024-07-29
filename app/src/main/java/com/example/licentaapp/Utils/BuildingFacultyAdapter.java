package com.example.licentaapp.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.licentaapp.R;
import java.util.Arrays;
import java.util.List;

public class BuildingFacultyAdapter extends RecyclerView.Adapter<BuildingFacultyAdapter.CorpFacultateViewHolder> {

    private List<BuildingFacultyModel> corpuriFacultate;
    private boolean[] isAdresaVizibila;

    public BuildingFacultyAdapter(List<BuildingFacultyModel> corpuriFacultate) {
        this.corpuriFacultate = corpuriFacultate;
        this.isAdresaVizibila = new boolean[corpuriFacultate.size()];
        Arrays.fill(isAdresaVizibila, false); // Setează toate elementele la false inițial
    }

    @NonNull
    @Override
    public CorpFacultateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.building_faculty_item, parent, false);
        return new CorpFacultateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CorpFacultateViewHolder holder, int position) {
        BuildingFacultyModel corpFacultate = corpuriFacultate.get(position);
        holder.textViewNumeCorp.setText(corpFacultate.getNumeCorpFacultate());

        // Verificați și setați vizibilitatea adresei
        if (isAdresaVizibila[position]) {
            holder.textViewAdresa.setVisibility(View.VISIBLE);
            holder.textViewAdresa.setText(corpFacultate.getAdresa());
        } else {
            holder.textViewAdresa.setVisibility(View.GONE);
        }

        // Adăugați un OnClickListener pentru itemView
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the adapter position dynamically
                int adapterPosition = holder.getAdapterPosition();

                // Ensure the position is valid
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // Invert the visibility of the address corresponding to the adapter position
                    isAdresaVizibila[adapterPosition] = !isAdresaVizibila[adapterPosition];

                    // Notify the adapter of the visibility change
                    notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return corpuriFacultate.size();
    }

    public void setCorpuriFacultate(List<BuildingFacultyModel> corpuriFacultate) {
        this.corpuriFacultate = corpuriFacultate;
        this.isAdresaVizibila = new boolean[corpuriFacultate.size()];
        Arrays.fill(isAdresaVizibila, false);
        notifyDataSetChanged();
    }

    static class CorpFacultateViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNumeCorp;
        TextView textViewAdresa;

        CorpFacultateViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumeCorp = itemView.findViewById(R.id.textViewNume);
            textViewAdresa = itemView.findViewById(R.id.textViewAdresa);
        }
    }
}

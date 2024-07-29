package com.example.licentaapp.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.licentaapp.R;
import java.util.List;

public class CaminAdapter extends RecyclerView.Adapter<CaminAdapter.CaminViewHolder> {

    private List<CaminModel> camine;

    public CaminAdapter(List<CaminModel> camine) {
        this.camine = camine;
    }

    public void setCamine(List<CaminModel> camine) {
        this.camine = camine;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CaminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camin_item, parent, false);
        return new CaminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaminViewHolder holder, int position) {
        CaminModel camin = camine.get(position);
        holder.textViewName.setText(camin.getNumeCamin());
        holder.textViewAddress.setText(camin.getAdresa());
        holder.textViewAdministrator.setText("Administrator: " + camin.getAdministrator());
        holder.textViewContact.setText("Contact");
        holder.textViewEmail.setText("Email: " + camin.getEmail());
        holder.textViewNrTelefon.setText("Nr. Telefon: " + camin.getNrTelefon());
        holder.textViewCapacitate.setText("Capacitate: " + camin.getCapacitate());

        // Adaugă OnClickListener pentru itemView
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Invert the visibility state
                if (holder.textViewAddress.getVisibility() == View.VISIBLE) {
                    holder.textViewAddress.setVisibility(View.GONE);
                    holder.textViewAdministrator.setVisibility(View.GONE);
                    holder.textViewContact.setVisibility(View.GONE);
                    holder.textViewEmail.setVisibility(View.GONE);
                    holder.textViewNrTelefon.setVisibility(View.GONE);
                    holder.textViewCapacitate.setVisibility(View.GONE);
                } else {
                    holder.textViewAddress.setVisibility(View.VISIBLE);
                    holder.textViewAdministrator.setVisibility(View.VISIBLE);
                    holder.textViewContact.setVisibility(View.VISIBLE);
                    holder.textViewEmail.setVisibility(View.VISIBLE);
                    holder.textViewNrTelefon.setVisibility(View.VISIBLE);
                    holder.textViewCapacitate.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return camine.size();
    }

    static class CaminViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewAddress;
        TextView textViewAdministrator;
        TextView textViewContact;
        TextView textViewEmail;
        TextView textViewNrTelefon;
        TextView textViewCapacitate;

        CaminViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewAdministrator = itemView.findViewById(R.id.textViewAdministrator);
            textViewContact = itemView.findViewById(R.id.textViewContact);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewNrTelefon = itemView.findViewById(R.id.textViewNrTelefon);
            textViewCapacitate = itemView.findViewById(R.id.textViewCapacitate);
        }
    }
}

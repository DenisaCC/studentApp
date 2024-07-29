package com.example.licentaapp.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.util.List;

public class UserProfessorAdapter extends RecyclerView.Adapter<UserProfessorAdapter.UserProfessorViewHolder> {

    private List<UserProfessorModel> professors;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    // Interfață pentru gestionarea evenimentului de ștergere
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    // Interfață pentru gestionarea evenimentului de editare
    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    // Metodă pentru setarea listener-ului pentru evenimentul de ștergere
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    // Metodă pentru setarea listener-ului pentru evenimentul de editare
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    // Constructor
    public UserProfessorAdapter() {
    }

    // Metodă pentru actualizarea datelor din adaptor
    public void setProfessors(List<UserProfessorModel> professors) {
        this.professors = professors;
        notifyDataSetChanged(); // Notifică RecyclerView că datele s-au schimbat
    }

    @NonNull
    @Override
    public UserProfessorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_professor, parent, false);
        return new UserProfessorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProfessorViewHolder holder, int position) {
        UserProfessorModel professor = professors.get(position);
        holder.bind(professor);

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

                // Afișează un dialog de confirmare pentru ștergere
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Sunteți sigur că doriți să ștergeți acest cont?")
                        .setCancelable(false)
                        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int adapterPosition = holder.getAdapterPosition();
                                if (deleteClickListener != null) {
                                    deleteClickListener.onDeleteClick(adapterPosition);
                                }
                            }
                        })
                        .setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (editClickListener != null) {
                    editClickListener.onEditClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return professors != null ? professors.size() : 0;
    }

    // ViewHolder pentru profesor
    static class UserProfessorViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewNumeUtilizator;
        private TextView textViewEmail;
        private ImageButton buttonDelete;
        private ImageButton buttonEdit;

        public UserProfessorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumeUtilizator = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
        }

        public void bind(UserProfessorModel professor) {
            textViewNumeUtilizator.setText(professor.getNumeUtilizator());
            textViewEmail.setText(professor.getEmail());
        }
    }
}

package com.example.licentaapp.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.util.List;

public class UserStudentAdapter extends RecyclerView.Adapter<UserStudentAdapter.UserStudentViewHolder> {

    private List<UserStudentModel> students;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int nrMatricol, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(UserStudentModel student, int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setStudents(List<UserStudentModel> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new UserStudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserStudentViewHolder holder, int position) {
        UserStudentModel student = students.get(position);
        holder.bind(student);

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
                                    deleteClickListener.onDeleteClick(student.getNrMatricol(), adapterPosition);
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
                if (editClickListener != null) {
                    editClickListener.onEditClick(student, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }

    static class UserStudentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUsername;
        private TextView textViewEmail;
        private ImageButton buttonDelete;
        private ImageButton buttonEdit;

        public UserStudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
        }

        public void bind(UserStudentModel student) {
            textViewUsername.setText(student.getUsername());
            textViewEmail.setText(student.getEmail());
        }
    }
}

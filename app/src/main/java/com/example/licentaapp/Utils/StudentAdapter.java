package com.example.licentaapp.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.R;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private Context context;
    private List<StudentModel> studentModelList;
    private SignInListener signInListener;
    private OnItemClickListener itemClickListener;

    public StudentAdapter(Context context, SignInListener signInListener) {
        this.context = context;
        this.studentModelList = new ArrayList<>();
        this.signInListener = signInListener;
    }

    public interface SignInListener {
        void onSignIn(String email, String password);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setStudentModelList(List<StudentModel> studentModelList) {
        this.studentModelList = studentModelList;
        notifyDataSetChanged();
    }

    public void add(StudentModel studentModel) {
        studentModelList.add(studentModel);
        notifyItemInserted(studentModelList.size() - 1);
    }

    public void clear() {
        studentModelList.clear();
        notifyDataSetChanged();
    }

    public StudentModel getItem(int position) {
        return studentModelList.get(position);
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_row, parent, false);
        return new StudentViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentModel studentModel = studentModelList.get(position);
        holder.studentName.setText(studentModel.getName());
        holder.studentYear.setText("An de studiu: " + studentModel.getYearOfStudy());
        holder.studentSpecialization.setText("Specializarea: " + studentModel.getSpecialization());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignInDialog(studentModel);
            }
        });
    }

    private void openSignInDialog(StudentModel studentModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_firebase_signin, null);
        builder.setView(dialogView);

        EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);

        builder.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    signInListener.onSignIn(email, password);
                } else {
                    Toast.makeText(context, "Email and password are required.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return studentModelList.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView studentName;
        TextView studentYear;
        TextView studentSpecialization;

        StudentViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            studentYear = itemView.findViewById(R.id.studentYear);
            studentSpecialization = itemView.findViewById(R.id.studentSpecialization);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}

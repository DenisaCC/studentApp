package com.example.licentaapp.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.licentaapp.ChatActivity;
import com.example.licentaapp.R;

import java.util.ArrayList;
import java.util.List;

public class ProfessorAdapter extends RecyclerView.Adapter<ProfessorAdapter.MyViewHolder> {

    private List<ProfessorModel> professorModelList;
    private Context context;

    private SignInListener signInListener;

    public ProfessorAdapter(Context context, SignInListener signInListener) {
        this.context = context;
        this.professorModelList = new ArrayList<>();
        this.signInListener = signInListener;
    }


    public void setProfessorModelList(List<ProfessorModel> professorModelList) {
        this.professorModelList = professorModelList;
        notifyDataSetChanged();
    }

    public void add(ProfessorModel professorModel){
        professorModelList.add(professorModel);
        notifyDataSetChanged();
    }

    public void clear(){
        professorModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.professor_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProfessorModel professorModel = professorModelList.get(position);
        holder.nameTextView.setText(professorModel.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignInDialog(professorModel);
                Intent intent = new Intent(context, ChatActivity.class);
            }
        });
    }

    @Override
    public int getItemCount() {
        return professorModelList.size();
    }

    public interface SignInListener {
        void onSignIn(String email, String password);
    }

    private void openSignInDialog(ProfessorModel professorModel) {
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
                    signInListener.onSignIn(email, password); // Apelați metoda onSignIn pentru a trimite datele către ChatMainActivity
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


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.professorName);
        }
    }
}
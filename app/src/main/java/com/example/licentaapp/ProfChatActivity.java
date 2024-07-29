package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.licentaapp.Utils.ChatAdapter;
import com.example.licentaapp.Utils.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Importurile rămân aceleași ca în exemplul tău
public class ProfChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mMessageInput;
    private ImageButton mSendMessageIcon, backBtn;
    private RecyclerView mMessageRecyclerView;
    private ChatAdapter mAdapter;
    private List<ChatMessage> mMessageList;

    private String studentUsername; // Numele de utilizator al studentului selectat din lista
    private String studentId; // ID-ul studentului pentru a trimite mesaje

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof_chat);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        mMessageInput = findViewById(R.id.messageInput);
        mSendMessageIcon = findViewById(R.id.sendMessageIcon);
        mMessageRecyclerView = findViewById(R.id.recycler);
        backBtn = findViewById(R.id.backBtn);

        // Get student username and ID from Intent
        Intent intent = getIntent();
        if (intent != null) {
            studentUsername = intent.getStringExtra("STUDENT_USERNAME");
            studentId = intent.getStringExtra("STUDENT_ID"); // Asume că aceasta este cheia pentru ID-ul studentului
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(studentUsername); // Set toolbar title to student's username
        }

        // Setup RecyclerView and Adapter
        mMessageList = new ArrayList<>();
        mAdapter = new ChatAdapter(mMessageList, mAuth.getCurrentUser().getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageRecyclerView.setLayoutManager(layoutManager);
        mMessageRecyclerView.setAdapter(mAdapter);

        // Back button click listener
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Deloghează utilizatorul din Firebase
                Intent intent = new Intent(ProfChatActivity.this, ProfChatMainActivity.class);
                intent.putExtra("USERNAME", studentUsername); // Send back student username
                startActivity(intent);
                finish();
            }
        });

        // Send message button click listener
        mSendMessageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Load existing messages from Firebase Database
        loadMessages();
    }

    private void loadMessages() {
        mDatabase.child("messages").orderByChild("receiverUsername").equalTo(studentUsername)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                        mMessageList.add(message);
                        mAdapter.notifyDataSetChanged();
                        mMessageRecyclerView.scrollToPosition(mMessageList.size() - 1);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }


    private void sendMessage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String messageText = mMessageInput.getText().toString().trim();
        String receiverId = studentId; // ID-ul studentului către care trimitem mesajul

        if (!TextUtils.isEmpty(messageText)) {
            String messageId = mDatabase.child("messages").push().getKey();
            ChatMessage message = new ChatMessage(messageId, currentUser.getUid(), currentUser.getEmail(), messageText, receiverId);

            Map<String, Object> messageValues = message.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/messages/" + messageId, messageValues);

            mDatabase.updateChildren(childUpdates)
                    .addOnSuccessListener(aVoid -> {
                        // Mesajul a fost trimis cu succes, poți adăuga acțiuni suplimentare aici
                        mMessageInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        // A apărut o eroare la trimiterea mesajului
                        Toast.makeText(ProfChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

}

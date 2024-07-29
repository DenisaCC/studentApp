package com.example.licentaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mMessageInput;
    private ImageButton mSendMessageIcon, backBtn;
    private RecyclerView mMessageRecyclerView;
    private ChatAdapter mAdapter;
    private List<ChatMessage> mMessageList;
    private String username, password, professorId;

    private static ChatActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        instance = this; // Setăm instanța curentă a ChatActivity

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMessageInput = findViewById(R.id.messageInput);
        mSendMessageIcon = findViewById(R.id.sendMessageIcon);
        mMessageRecyclerView = findViewById(R.id.recycler);
        mMessageList = new ArrayList<>();
        mAdapter = new ChatAdapter(mMessageList, mAuth.getCurrentUser().getUid());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageRecyclerView.setLayoutManager(layoutManager);
        mMessageRecyclerView.setAdapter(mAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("USERNAME");
            password = intent.getStringExtra("PASSWORD");
            professorId = intent.getStringExtra("PROFESSOR_ID"); // Obținem ID-ul profesorului
        }

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Deloghează utilizatorul din Firebase
                Intent intent = new Intent(ChatActivity.this, ChatMainActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("PASSWORD", password);
                startActivity(intent);
                finish();
            }
        });

        mSendMessageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        loadMessagesForProfessor(professorId); // Încărcăm mesajele pentru profesorul specificat

        // Abonarea la un topic pentru a primi notificări
        subscribeToNotificationTopic();
    }

    private void subscribeToNotificationTopic() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String topic = "user_" + currentUser.getUid(); // Crează un topic unic bazat pe ID-ul utilizatorului Firebase
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("ChatActivity", "Abonare la topic reușită: " + topic);
                        } else {
                            Log.w("ChatActivity", "Abonare la topic eșuată: " + topic, task.getException());
                        }
                    });
        }
    }

    public static ChatActivity getInstance() {
        return instance;
    }

    public void showNewMessageIcon() {
        // Implementează afișarea iconiței pentru mesaje noi în interfață
        // Exemplu: poți actualiza o iconiță sau o notificare vizuală în interfață
        // Acest cod va fi specific aplicației tale și cum dorești să afișezi mesajele noi
        Toast.makeText(this, "New message received!", Toast.LENGTH_SHORT).show();
    }

    private void loadMessagesForProfessor(String professorId) {
        mDatabase.child("messages").orderByChild("professorId").equalTo(professorId).addChildEventListener(new ChildEventListener() {
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
        String receiverId = getIntent().getStringExtra("PROFESSOR_ID");

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
                        Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }
}

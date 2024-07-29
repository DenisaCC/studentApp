package com.example.licentaapp.Utils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.licentaapp.R;
import java.util.List;
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatMessage> mMessages;
    private String currentUserUid;

    public ChatAdapter(List<ChatMessage> messages, String currentUserUid) {
        mMessages = messages;
        this.currentUserUid = currentUserUid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Determinăm layout-ul în funcție de expeditorul mesajului
        if (viewType == 0) {
            // Mesaj trimis de utilizatorul curent
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            // Mesaj primit
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = mMessages.get(position);
        // Verificăm dacă mesajul a fost trimis de utilizatorul curent sau nu
        if (message.getUserId().equals(currentUserUid)) {
            return 0; // Mesaj trimis de utilizatorul curent
        } else {
            return 1; // Mesaj primit
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(ChatMessage message) {
            messageTextView.setText(message.getText());
        }
    }
}

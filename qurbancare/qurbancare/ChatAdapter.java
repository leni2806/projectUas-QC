package com.example.qurbancare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<ChatModel> chatList;

    public ChatAdapter(List<ChatModel> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Mengambil layout balon chat yang sudah kita buat
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatModel chat = chatList.get(position);

        if (chat.sentBy.equals(ChatModel.SENT_BY_ME)) {
            // Jika pesan dari Leni: Sembunyikan kiri (AI), Munculkan kanan (Leni)
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightChatText.setText(chat.message);
        } else {
            // Jika pesan dari AI: Sembunyikan kanan (Leni), Munculkan kiri (AI)
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.leftChatText.setText(chat.message);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // Kita panggil TextView dan CardView (pembungkusnya)
        TextView leftChatText, rightChatText;
        View leftChatView, rightChatView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi ID sesuai dengan item_chat.xml yang premium
            leftChatText = itemView.findViewById(R.id.left_chat_text);
            rightChatText = itemView.findViewById(R.id.right_chat_text);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
        }
    }
}
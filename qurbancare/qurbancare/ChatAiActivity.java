package com.example.qurbancare;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class ChatAiActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private EditText etInputChat;
    private ChatAdapter chatAdapter;
    private List<ChatModel> chatList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        etInputChat = findViewById(R.id.etInputChat);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        ImageButton btnKirim = findViewById(R.id.btnKirim);

        chatAdapter = new ChatAdapter(chatList);
        recyclerViewChat.setAdapter(chatAdapter);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));

        btnKirim.setOnClickListener(v -> {
            String msg = etInputChat.getText().toString().trim();
            if (!msg.isEmpty()) {
                tambahChat(msg, ChatModel.SENT_BY_ME);
                etInputChat.setText("");
                panggilAI(msg);
            }
        });

        if (findViewById(R.id.btnBackChat) != null) {
            findViewById(R.id.btnBackChat).setOnClickListener(v -> finish());
        }
    }

    private void panggilAI(String prompt) {
        tambahChat("AI sedang mengetik...", ChatModel.SENT_BY_BOT);
        final int loadingIndex = chatList.size() - 1;

        // API Key Leni (Pastikan sudah verifikasi email di Groq ya!)
        String apiKey = "";

        try {
            JSONObject jsonBody = new JSONObject();
            // Menggunakan model terbaru yang paling stabil
            jsonBody.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", prompt);
            messages.put(messageObject);

            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    updateAIPesan(loadingIndex, "Koneksi Gagal. Cek sinyal HP.");
                    Log.e("AI_ERROR", "Gagal: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    String res = response.body().string();
                    Log.d("AI_DEBUG", "Status: " + response.code() + " Body: " + res);

                    if (response.isSuccessful()) {
                        try {
                            JSONObject jo = new JSONObject(res);
                            String hasil = jo.getJSONArray("choices").getJSONObject(0)
                                    .getJSONObject("message").getString("content");
                            updateAIPesan(loadingIndex, hasil);
                        } catch (Exception e) {
                            updateAIPesan(loadingIndex, "Gagal memproses jawaban.");
                        }
                    } else {
                        // Jika Error 400 tetap muncul, ini pesan yang muncul di chat
                        updateAIPesan(loadingIndex, "Server Sibuk (Error " + response.code() + ")");
                        Log.e("AI_ERROR", "Detail: " + res);
                    }
                }
            });

        } catch (Exception e) {
            updateAIPesan(loadingIndex, "Gagal menyusun pesan.");
        }
    }

    private void updateAIPesan(int index, String pesanBaru) {
        runOnUiThread(() -> {
            if (index >= 0 && index < chatList.size()) {
                chatList.set(index, new ChatModel(pesanBaru, ChatModel.SENT_BY_BOT));
                chatAdapter.notifyItemChanged(index);
                recyclerViewChat.scrollToPosition(index);
            }
        });
    }

    private void tambahChat(String msg, String pengirim) {
        chatList.add(new ChatModel(msg, pengirim));
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        recyclerViewChat.scrollToPosition(chatList.size() - 1);
    }
}
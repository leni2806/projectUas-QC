package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity2 extends AppCompatActivity {

    private TextView tvTyping;
    private String fullText = "Qurban Care";
    private int index = 0;
    private long delay = 150; // Kecepatan ngetik (ms)
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);

        tvTyping = findViewById(R.id.tvTyping);
        mAuth = FirebaseAuth.getInstance();

        // Mulai efek mengetik
        startTypingAnimation();
    }

    private void startTypingAnimation() {
        Handler handler = new Handler();
        handler.post(new Runnable() { // Pakai .post() untuk mulai langsung
            @Override
            public void run() {
                if (index <= fullText.length()) {
                    // Update teks
                    tvTyping.setText(fullText.substring(0, index));
                    index++;
                    handler.postDelayed(this, delay);
                } else {
                    // Jika sudah selesai ngetik, tunggu 1 detik lalu pindah halaman
                    new Handler().postDelayed(() -> {

                        // 🔥 LOGIKA PINDAH HALAMAN
                        Intent intent;
                        if (mAuth.getCurrentUser() != null) {
                            // Jika sudah pernah login, langsung ke Dashboard
                            intent = new Intent(SplashActivity2.this, SapaActivity.class);
                        } else {
                            // Jika belum login, ke halaman Login
                            intent = new Intent(SplashActivity2.this, OnboardingActivity.class);
                        }

                        startActivity(intent);

                        // Efek transisi halus
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }, 1000);
                }
            }
        });
    }
}
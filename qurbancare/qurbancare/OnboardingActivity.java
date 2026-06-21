package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ImageButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Biar user tau tombol sudah diklik, kita kasih delay sebentar
                // 🔥 LOGIKA DELAY 1 DETIK (1000 milidetik)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Pindah ke TransparencyActivity
                        Intent intent = new Intent(OnboardingActivity.this, TransparencyActivity.class);
                        startActivity(intent);

                        // Ef To transisi halus (Fade)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                        // Tutup halaman onboarding agar tidak bisa di-back
                        finish();
                    }
                }, 1000); // 1000ms = 1 Detik
            }
        });
    }
}
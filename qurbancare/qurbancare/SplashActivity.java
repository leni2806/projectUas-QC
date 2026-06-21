package com.example.qurbancare;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo, oval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        oval = findViewById(R.id.oval);

        // Keadaan Awal
        oval.setScaleX(0f);
        oval.setScaleY(0f);
        logo.setAlpha(0f);
        logo.setTranslationY(100f);

        startSplashAnimation();
    }

    private void startSplashAnimation() {
        // --- 1. OVAL MUNCUL ---
        ObjectAnimator ovalScaleX = ObjectAnimator.ofFloat(oval, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator ovalScaleY = ObjectAnimator.ofFloat(oval, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator ovalAlpha = ObjectAnimator.ofFloat(oval, "alpha", 0f, 1f);

        AnimatorSet showOval = new AnimatorSet();
        showOval.playTogether(ovalScaleX, ovalScaleY, ovalAlpha);
        showOval.setDuration(600);

        // --- 2. LOGO NAIK ---
        ObjectAnimator logoUp = ObjectAnimator.ofFloat(logo, "translationY", 100f, 0f);
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);

        AnimatorSet showLogo = new AnimatorSet();
        showLogo.playTogether(logoUp, logoFadeIn);
        showLogo.setDuration(800);

        // --- 3. LOGO MASUK KE OVAL ---
        ObjectAnimator logoDown = ObjectAnimator.ofFloat(logo, "translationY", 0f, 200f);
        ObjectAnimator logoFadeOut = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f);
        ObjectAnimator ovalScaleDownX = ObjectAnimator.ofFloat(oval, "scaleX", 1f, 0f);
        ObjectAnimator ovalScaleDownY = ObjectAnimator.ofFloat(oval, "scaleY", 1f, 0f);

        AnimatorSet hideAll = new AnimatorSet();
        hideAll.playTogether(logoDown, logoFadeOut, ovalScaleDownX, ovalScaleDownY);
        hideAll.setDuration(700);
        hideAll.setStartDelay(1000); // Tahan 1 detik biar logo kelihatan

        // --- GABUNGKAN ---
        AnimatorSet finalAnim = new AnimatorSet();
        finalAnim.playSequentially(showOval, showLogo, hideAll);
        finalAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        finalAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 🔥 DELAY 1 DETIK SEBELUM PINDAH KE SPLASH 2
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(SplashActivity.this, SplashActivity2.class);
                    startActivity(intent);

                    // 🔥 EFEK KEDIP (Bukan geser)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }, 1000);
            }

            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        finalAnim.start();
    }
}
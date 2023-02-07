package com.example.humancheckers.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;

import com.example.humancheckers.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the default UI
        getSupportActionBar().hide();

        setContentView(R.layout.activity_splash);

        // get the imageviews
        ImageView logo = findViewById(R.id.splash_IMG_logo);
        ImageView img1 = findViewById(R.id.splash_IMG_img1);
        ImageView img2 = findViewById(R.id.splash_IMG_img2);
        ImageView img3 = findViewById(R.id.splash_IMG_img3);
        ImageView img4 = findViewById(R.id.splash_IMG_img4);

        // reset all the scales to start scale 0
        logo.setScaleX(0); logo.setScaleY(0);
        img1.setScaleX(0); img1.setScaleY(0);
        img2.setScaleX(0); img2.setScaleY(0);
        img3.setScaleX(0); img3.setScaleY(0);
        img4.setScaleX(0); img4.setScaleY(0);

        startAnimationIMG(img1);
        new Handler().postDelayed(() -> startAnimationIMG(img2), 300);
        new Handler().postDelayed(() -> startAnimationIMG(img3), 600);
        new Handler().postDelayed(() -> startAnimationIMG(img4), 900);
        new Handler().postDelayed(() -> startAnimationLogo(logo), 1200);
    }

    private void startAnimationIMG(ImageView iv) {
        ViewPropertyAnimator anim = iv.animate();
        anim.rotation(360).scaleX(1.5f).scaleY(1.5f).setDuration(1500).start();
        MediaPlayer.create(this, R.raw.bubble).start();
    }

    private void startAnimationLogo(ImageView logo) {
        ViewPropertyAnimator anim = logo.animate();
        anim.scaleX(1.2f).scaleY(1.2f).setDuration(800).withEndAction(() -> {
            anim.scaleX(1).scaleY(1).setDuration(300).withEndAction(() -> {
                new Handler().postDelayed( () -> {
                    Intent intent = new Intent(this, FirebaseUIActivity.class);
                    startActivity(intent);
                    finish();
                }, 1000);
            }).start();
        }).start();
        MediaPlayer.create(this, R.raw.bubble).start();
    }
}
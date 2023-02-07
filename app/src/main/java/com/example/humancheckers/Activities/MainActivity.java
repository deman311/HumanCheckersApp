package com.example.humancheckers.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.humancheckers.MusicService;
import com.example.humancheckers.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the default UI
        getSupportActionBar().hide();

        FirebaseUser user = (FirebaseUser) getIntent().getExtras().get("user");
        setContentView(R.layout.activity_main);
        TextView prompt = findViewById(R.id.main_TXT_username);
        prompt.setText("Welcome " + user.getEmail() + "!");

        // define main menu buttons
        MaterialButton singleplayerBtn = (MaterialButton) findViewById(R.id.main_BTN_singleplayer);
        MaterialButton multiplayerBtn = (MaterialButton) findViewById(R.id.main_BTN_multiplayer);
        MaterialButton leaderboardsBtn = (MaterialButton) findViewById(R.id.main_BTN_leaderboards);

        singleplayerBtn.setOnClickListener(cl -> {
            cl.setEnabled(false); // click spamming safety
            Intent pick = new Intent(this, SelectActivity.class);
            pick.putExtra("isSingle", true);
            pick.putExtra("user", user);
            startActivity(pick);
        });
        multiplayerBtn.setOnClickListener(cl -> {
            cl.setEnabled(false); // click spamming safety
            Intent multi = new Intent(this, WaitroomActivity.class);
            multi.putExtra("isSingle", false);
            multi.putExtra("user", user);
            startActivity(multi);
        });
        leaderboardsBtn.setOnClickListener(cl -> {
            cl.setEnabled(false); // click spamming safety
            Intent highscores = new Intent(this, HighscoreActivity.class);
            startActivity(highscores);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(new Intent(this, MusicService.class));

        // reset button enable
        MaterialButton singleplayerBtn = (MaterialButton) findViewById(R.id.main_BTN_singleplayer);
        MaterialButton multiplayerBtn = (MaterialButton) findViewById(R.id.main_BTN_multiplayer);
        MaterialButton leaderboardsBtn = (MaterialButton) findViewById(R.id.main_BTN_leaderboards);
        singleplayerBtn.setEnabled(true);
        multiplayerBtn.setEnabled(true);
        leaderboardsBtn.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
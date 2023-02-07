package com.example.humancheckers.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.humancheckers.Managers.PickManager;
import com.example.humancheckers.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the default UI
        getSupportActionBar().hide();

        setContentView(R.layout.activity_select);

        Boolean isSingle = getIntent().getBooleanExtra("isSingle", false);
        FirebaseUser user = (FirebaseUser) getIntent().getExtras().get("user");
        int pNum = getIntent().getIntExtra("pNum", 1);
        String roomid = getIntent().getStringExtra("id");

        // get buttons
        ImageButton left = findViewById(R.id.pick_BTN_left);
        ImageButton right = findViewById(R.id.pick_BTN_right);
        ImageButton choice = findViewById(R.id.pick_BTN_choice);

        PickManager pickManager = new PickManager();
        ImageView iv = findViewById(R.id.pick_IMG_heroimg);
        TextView tv = findViewById(R.id.pick_TXT_heroname);

        // define default
        iv.setImageResource(pickManager.currentSkin());
        tv.setText(pickManager.currentSkinName());

        // define button logic
        left.setOnClickListener(cl -> {
            iv.setImageResource(pickManager.leftSkin());
            tv.setText(pickManager.currentSkinName());
        });
        right.setOnClickListener(cl -> {
            iv.setImageResource(pickManager.rightSkin());
            tv.setText(pickManager.currentSkinName());
        });
        choice.setOnClickListener(cl -> {
            cl.setEnabled(false); // click spamming safety
            swapToTilePicker(left, right, choice, pickManager, iv, tv,
                    isSingle, pickManager.chooseSkin(), pNum, roomid, user);
        });
    }

    private void swapToTilePicker(ImageButton left, ImageButton right, ImageButton choice,
                                  PickManager pickManager, ImageView iv, TextView tv,
                                  boolean isSingle, int skinChoice, int pNum, String roomid, FirebaseUser user) {
        TextView title = findViewById(R.id.pick_TXT_title);
        title.setText("Pick a map tile");
        iv.setImageResource(pickManager.currentTile());
        tv.setText(pickManager.currentTileName());

        // define button logic
        left.setOnClickListener(cl -> {
            iv.setImageResource(pickManager.leftTile());
            tv.setText(pickManager.currentTileName());
        });
        right.setOnClickListener(cl -> {
            iv.setImageResource(pickManager.rightTile());
            tv.setText(pickManager.currentTileName());
        });
        choice.setOnClickListener(cl -> {
            cl.setEnabled(false); // click spamming safety
            cl.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            left.setEnabled(false);
            left.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            right.setEnabled(false);
            right.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            Intent game = new Intent(this, GameActivity.class);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (!isSingle) {
                game.putExtra("isSingle", false);
                game.putExtra("roomID", roomid);
                game.putExtra("pNum", pNum);
                game.putExtra("user", user);

                // push my data to the DB
                db.collection("rooms").document(roomid).get().addOnSuccessListener(sl -> {
                    Map<String, String> data = new HashMap();
                    data.put("skinP" + pNum, ""+skinChoice);
                    data.put("tileP" + pNum, ""+pickManager.chooseTile());
                    data.put("user" + pNum, user.getEmail().substring(0, user.getEmail().indexOf("@")));

                    if (sl.get("status").equals("picked")) {
                        data.put("status", "ready");
                        sl.getReference().set(data, SetOptions.merge());
                        startActivity(game);
                        finish();
                    }
                    else {
                        data.put("status", "picked");
                        sl.getReference().set(data, SetOptions.merge());
                        Toast.makeText(this, "Awaiting other player", Toast.LENGTH_SHORT).show();
                        sl.getReference().addSnapshotListener((snl, ex) -> {
                            if (snl.exists() && snl.get("status").equals("ready")) {
                                startActivity(game);
                                finish();
                            }
                        });
                    }
                });
            }
            else {
                game.putExtra("skinP1", skinChoice);
                game.putExtra("tileP1", pickManager.chooseTile());
                game.putExtra("skinP2", pickManager.getRandomSkin());
                game.putExtra("tileP2", pickManager.getRandomTile());
                game.putExtra("isSingle", true);
                game.putExtra("user", user);
                startActivity(game);
                finish();
            }
        });
        choice.setEnabled(true); // re-enable the button only after finishing define
    }
}
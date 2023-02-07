package com.example.humancheckers.Activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.humancheckers.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WaitroomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitroom);

        // Hide the default UI
        getSupportActionBar().hide();

        EditText input = findViewById(R.id.wr_ET_roomid);
        MaterialButton joinBtn = findViewById(R.id.wr_BTN_join);
        MaterialButton createBtn = findViewById(R.id.wr_BTN_create);

        FirebaseUser user = (FirebaseUser) getIntent().getExtras().get("user");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        createBtn.setOnClickListener(cl -> {
            // disable the ability to make changes
            input.setEnabled(false);
            joinBtn.setEnabled(false);
            joinBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            createBtn.setEnabled(false);
            createBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            // generate a random ID containing 6 alphabets
            Random rand = new Random();
            String id = "";
            for (int i = 0; i < 4; i++) {
                int c = rand.nextInt(36);
                if (c > 25) // number
                    id += (35 - c); // 0-9
                else
                    id += (char) ('a' + c); // a-z
            }
            // [] VALIDATE ROOM ID DOES NOT ALREADY EXIST IN THE FIRESTORE

            input.setText(id); // show the id generated for the player to share

            // upload room to firestore
            Map<String, String> data = new HashMap<>();
            data.put("id", id);
            data.put("player1", user.getEmail());
            data.put("status", "waiting");
            db.collection("rooms").document(id).set(data);

            // start listener
            String finalId = id;
            db.collection("rooms").document(id).addSnapshotListener((snl, ex) -> {
                if (snl != null && snl.exists()) {
                    if (snl.get("status").equals("complete")) {
                        Intent intent = new Intent(this, SelectActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("isSingle", false);
                        intent.putExtra("pNum", 1);
                        intent.putExtra("id", finalId);
                        startActivity(intent);
                        finish();
                    }
                }
            });
            Toast.makeText(this, "Waiting for player2", Toast.LENGTH_SHORT).show();
        });

        joinBtn.setOnClickListener(cl -> {
            // disable the ability to make changes
            input.setEnabled(false);
            joinBtn.setEnabled(false);
            joinBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            createBtn.setEnabled(false);
            createBtn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            String id = input.getText().toString();
            if (!id.isEmpty()) {
                db.collection("rooms").get().addOnSuccessListener(sl -> {
                    boolean hasFound = false;
                    for (DocumentSnapshot document : sl.getDocuments())
                        if (document.get("id", String.class).equals(id)) {
                            if (!document.get("status").equals("waiting")) {
                                // room taken
                                Toast.makeText(this, "Room is already taken!", Toast.LENGTH_SHORT).show();
                                return; // stop checking
                            } else {
                                // room ready
                                hasFound = true;
                                Map<String, String> data = new HashMap<>();
                                data.put("id", id);
                                data.put("player2", user.getEmail());
                                data.put("status", "complete");
                                db.collection("rooms").document(id).set(data, SetOptions.merge());
                                Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
                                db.collection("rooms").document(id).addSnapshotListener((sl2, ex) -> {
                                    if (sl2 != null && sl2.exists()) {
                                        if (sl2.get("status").equals("complete")) {
                                            Intent intent = new Intent(this, SelectActivity.class);
                                            intent.putExtra("user", user);
                                            intent.putExtra("isSingle", false);
                                            intent.putExtra("pNum", 2);
                                            intent.putExtra("id", id);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    if (!hasFound)
                        Toast.makeText(this, "Wrong room ID!", Toast.LENGTH_SHORT).show();
                });
            } else
                Toast.makeText(this, "Must enter an id!", Toast.LENGTH_SHORT).show();

            // reset disable
            input.setEnabled(true);
            joinBtn.setEnabled(true);
            joinBtn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            createBtn.setEnabled(true);
            createBtn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        });
    }
}
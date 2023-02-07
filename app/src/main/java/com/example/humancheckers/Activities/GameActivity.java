package com.example.humancheckers.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.humancheckers.Fragments.BoardFragment;
import com.example.humancheckers.Fragments.GameFragment;
import com.example.humancheckers.Models.Player;
import com.example.humancheckers.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the default UI
        getSupportActionBar().hide();

        GameFragment.isSingle = getIntent().getBooleanExtra("isSingle", true);
        if (GameFragment.isSingle) {
            // get extras passed by pick menu
            GameFragment.username1 = "player1";
            GameFragment.username2 = "player2";
            GameFragment.skinP1 = getIntent().getIntExtra("skinP1", -1);
            GameFragment.tile1 = getIntent().getIntExtra("tileP1", -1);
            GameFragment.skinP2 = getIntent().getIntExtra("skinP2", -1);
            GameFragment.tile2 = getIntent().getIntExtra("tileP2", -1);
        }
        else {
            String roomid = getIntent().getStringExtra("roomID");
            GameFragment.tileManager.roomid = roomid;

            int pNum = getIntent().getIntExtra("pNum", 0);
            FirebaseUser user = (FirebaseUser) getIntent().getExtras().get("user");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("rooms").document(roomid).get().addOnSuccessListener(sl -> {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
                DatabaseReference tlRef = database.getReference(roomid).child("turnOwner");
                DatabaseReference moveRef = database.getReference(roomid).child("move");
                DatabaseReference bombRef = database.getReference(roomid).child("bomb");
                DatabaseReference conRef = database.getReference(roomid).child("connection");

                setRefListeners(tlRef, moveRef, bombRef, conRef);
                if (pNum == 1) // client 1 initializes the value
                    tlRef.setValue(new Random().nextInt(2) + 1); // 1 or 2
            });
        }
        setContentView(R.layout.activity_game);

        if (GameFragment.isSingle)
            ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.game_fragment)).initFragment();
    }

    private void setRefListeners(DatabaseReference tlRef, DatabaseReference moveRef, DatabaseReference bombRef, DatabaseReference conRef) {
        // called every turn switch
        tlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.exists()) {
                    int value = dataSnapshot.getValue(Integer.class);
                    GameFragment.setTurnOwner(value);
                    String pName = value == 1 ? GameFragment.username1 : GameFragment.username2;
                    ((BoardFragment) getSupportFragmentManager().findFragmentById(R.id.board_fragment)).playerPrompt(pName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // called every move
        moveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (GameFragment.tileManager.isUpdating)
                    GameFragment.tileManager.isUpdating = false;
                else if (dataSnapshot.exists()) {
                    List<Long> data = (List<Long>) dataSnapshot.getValue();
                    ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.game_fragment))
                        .updateMove(data.get(0).intValue(), data.get(1).intValue(), data.get(2).intValue(), data.get(3).intValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // called every bomb usage
        bombRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (GameFragment.tileManager.isUpdating)
                    GameFragment.tileManager.isUpdating = false;
                else if (dataSnapshot.exists()) {
                    List<Long> data = (List<Long>) dataSnapshot.getValue();
                    int r1 = data.get(0).intValue();
                    int c1 = data.get(1).intValue();
                    int r2 = data.get(2).intValue();
                    int c2 = data.get(3).intValue();
                    GameFragment.tileManager.checkerMap[r1][c1].imageView.setVisibility(View.INVISIBLE);
                    ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.game_fragment)).bombSoundAndAnimation(r2, c2);
                    ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.game_fragment)).checkEndGame();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // connection updates of the opposite player
        conRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.exists()) {
                    switch (dataSnapshot.getValue(String.class)) {
                        case "closed":
                            Toast.makeText(GameActivity.this, "A player has quit the game!", Toast.LENGTH_SHORT).show();
                            dataSnapshot.getRef().removeValue();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("rooms").document(GameFragment.tileManager.roomid).get().addOnSuccessListener(sl -> sl.getReference().delete());
                            finish();
                            break;
                        case "paused":
                            Toast.makeText(GameActivity.this, "A player has paused the game!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!GameFragment.isSingle) {
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference conRef = database.getReference(GameFragment.tileManager.roomid).child("connection");
            conRef.setValue("paused");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!GameFragment.isSingle) {
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference conRef = database.getReference(GameFragment.tileManager.roomid).child("connection");
            conRef.setValue("closed");
        }

        // clear all and go back to main menu
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", (FirebaseUser) getIntent().getExtras().get("user")); // forward the user
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
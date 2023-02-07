package com.example.humancheckers.Activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.humancheckers.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HighscoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        // Hide the default UI
        getSupportActionBar().hide();

        GridLayout scores = findViewById(R.id.lb_GRL_scores);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("highscores")
                .orderBy("score", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(x -> {
                    for (DocumentSnapshot doc : x.getDocuments()) {
                        TextView tv = new TextView(this);
                        tv.setText(doc.getLong("score") + " | " + doc.getId() + " | "
                                + doc.getTimestamp("date").toDate());
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = GridLayout.LayoutParams.MATCH_PARENT;
                        tv.setLayoutParams(params);
                        tv.setGravity(Gravity.CENTER);
                        tv.setPadding(10, 10, 10, 10);
                        scores.addView(tv);
                    }
                });
    }
}
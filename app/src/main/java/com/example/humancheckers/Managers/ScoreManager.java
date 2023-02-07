package com.example.humancheckers.Managers;

import android.widget.TextView;

public class ScoreManager {
    private int score;
    private TextView scoreText;

    public ScoreManager(TextView text) {
        score = 0;
        scoreText = text;
    }

    public void eatScore() {
        setScore(50);
    }

    public void moveScore() {
        setScore(-1);
    }

    public void bombScore() {
        setScore(10);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int delta) {
        score += delta;
        if (score < 0)
            score = 0;
        scoreText.setText("" + score);
    }
}

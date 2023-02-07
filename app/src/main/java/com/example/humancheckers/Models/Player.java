package com.example.humancheckers.Models;

import com.example.humancheckers.R;

public class Player {
    private String name;
    private int skin;
    private int score;

    public Player() { score = 0; name = "BobTheBot"; skin = R.drawable.chk_warrior; } // default

    public Player setName(String name) {
        this.name = name;
        return this;
    }
    public Player setSkin(int skin) {
        this.skin = skin;
        return this;
    }
    public Player setScore(int score) {
        this.score = score;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getSkin() {
        return skin;
    }

    public int getScore() {
        return score;
    }
}

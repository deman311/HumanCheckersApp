package com.example.humancheckers.Models;

import android.view.View;
import android.widget.ImageView;

public class Tile {
    public ImageView imageView;
    public boolean isMarked;
    public boolean isPotential;
    private Checker stander;

    public Tile(ImageView iv) {
        imageView = iv;
        isMarked = false;
        isPotential = false;
        stander = null;
    }

    public boolean isEmpty() {
        return stander == null || stander.imageView.getVisibility() == View.INVISIBLE;
    }

    public Checker getStander() {
        return stander;
    }

    public void setStander(Checker checker) {
        stander = checker;
    }
}

package com.example.humancheckers.Models;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageView;

public class Checker {
    public ImageView imageView;
    public boolean isMarked;
    public boolean isTop;
    public int row;
    public int col;

    public Checker(ImageView iv, boolean isTop, int row, int col) {
        imageView = iv;
        isMarked = false;
        this.isTop = isTop;
        this.row = row;
        this.col = col;
    }

    public void toggleMark() {
        isMarked = !isMarked;

        if (isMarked)
            imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        else
            imageView.setColorFilter(Color.TRANSPARENT);
    }
}

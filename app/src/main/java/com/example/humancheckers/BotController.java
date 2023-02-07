package com.example.humancheckers;

import android.view.View;
import android.widget.ImageView;

import com.example.humancheckers.Managers.TileManager;

import java.util.ArrayList;
import java.util.Random;

public class BotController {
    public TileManager tileManager;

    public BotController(TileManager manager) {
        tileManager = manager;
    }

    public void makeTurn() {
        // get all the available checkers
        ArrayList<ImageView> availableCheckers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tileManager.checkerMap[i][j].imageView.getVisibility() == View.VISIBLE
                    && tileManager.checkerMap[i][j].imageView.getTag().equals("p2"))
                    availableCheckers.add(tileManager.checkerMap[i][j].imageView);
            }
        }
        // click on random checker
        do {
            availableCheckers.remove(new Random().nextInt(availableCheckers.size())).callOnClick();
        } while (!tileManager.checkForMarks());

        // click on random tile
        ArrayList<ImageView> availableTiles = new ArrayList<>();    // get all the marked tile
        do {
            // get all the marked tile
            availableTiles.clear();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (tileManager.tileMap[i][j].isMarked)
                        availableTiles.add(tileManager.tileMap[i][j].imageView);
                }
            }
            availableTiles.get(new Random().nextInt(availableTiles.size())).callOnClick();
        } while(tileManager.checkForMarks());

        // check if bomb reached
        for (int k = 0; k < 8; k++) {
            if (tileManager.checkerMap[0][k].imageView.getVisibility() == View.VISIBLE
                && tileManager.checkerMap[0][k].imageView.getTag().equals("p2")) {
                // pick a random enemy checker and kill
                availableCheckers.clear();
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (tileManager.checkerMap[i][j].imageView.getVisibility() == View.VISIBLE
                                && tileManager.checkerMap[i][j].imageView.getTag().equals("p1"))
                            availableCheckers.add(tileManager.checkerMap[i][j].imageView);
                    }
                }
                availableCheckers.get(new Random().nextInt(availableCheckers.size())).callOnClick();
                break;
            }
        }
    }
}

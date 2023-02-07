package com.example.humancheckers.Managers;

import static com.example.humancheckers.Fragments.GameFragment.tileManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.humancheckers.Fragments.GameFragment;
import com.example.humancheckers.Models.Checker;
import com.example.humancheckers.Models.Tile;
import com.example.humancheckers.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileManager {
    public Tile[][] tileMap;
    public Checker[][] checkerMap;
    public int skinP1, skinP2;
    public int player; // defines the player ID on the device
    public int turnOwner;
    public static String roomid;
    public static boolean isUpdating;

    public TileManager(int skinP1, int skinP2) {
        tileMap = new Tile[8][8];
        checkerMap = new Checker[8][8];
        this.skinP1 = skinP1;
        this.skinP2 = skinP2;

        player = 1; // default value
        turnOwner = new Random().nextInt(2) + 1; // randomly assign starting player
    }

    public void switchTurn(boolean isSingle, GameFragment game) {
        if (turnOwner == 1)
            turnOwner = 2;
        else
            turnOwner = 1;

        if (!hasMoves(game)) {
            game.promptStuck(); // really fast won't see the prompt
            switchTurn(isSingle, game);
        }

        if (!isSingle) {
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference turnRef = database.getReference(roomid).child("turnOwner");
            turnRef.setValue(turnOwner);
        }
    }

    public boolean isMyTurn() {
        return player == turnOwner;
    }

    public void setTile(int row, int col, ImageView iv) {
        tileMap[row][col] = new Tile(iv);
    }

    public void setChecker(int row, int col, ImageView iv, boolean isTop) {
        checkerMap[row][col] = new Checker(iv, isTop, row, col);
    }

    public void displayMoveHelper(Checker marked) {
        int r = marked.row, c = marked.col;
        int rowOffset = marked.isTop ? 1 : -1;
        if ((marked.isTop && r < 7) || (!marked.isTop && r > 0)) {
            if (c > 0 && tileMap[r + rowOffset][c - 1].isEmpty())
                tileMarkToggle(tileMap[r + rowOffset][c - 1]);
            if (c < 7 && tileMap[r + rowOffset][c + 1].isEmpty())
                tileMarkToggle(tileMap[r + rowOffset][c + 1]);
        }
        checkEat(marked, true, marked.imageView.getTag(), 0);
    }

    /**
     * Scan the tilemap in relation to a checker and display potential 'Eat' moves on the enemy.
     * @param checker The checker in relation to whom to scan moves.
     * @param isFirst Is it the first move?
     * @param tag The tag of the initial checker.
     * @param state 0 state is for regular moves. -1 states that this is scan is an ongoing chain
     *              move and therefore it should not mark as potential but treat as first move
     *              while scanning in all directions.
     */
    private void checkEat(Checker checker, boolean isFirst, Object tag, int state) {
        int r = checker.row, c = checker.col;
        if (isFirst) {
            boolean isTop = checker.isTop;
            int rowOffset = isTop ? 2 : -2;
            if ((isTop && r < 6) || (!isTop && r > 1)) {
                if (c > 1
                        && !tileMap[r + rowOffset / 2][c - 1].isEmpty()
                        && checkerMap[r + rowOffset / 2][c - 1].imageView.getTag() != tag
                        && tileMap[r + rowOffset][c - 2].isEmpty()) {
                    tileMarkToggle(tileMap[r + rowOffset][c - 2]);
                    checkEat(checkerMap[r + rowOffset][c - 2], false, tag, isTop ? 3 : 4);
                }
                if (c < 6
                        && !tileMap[r + rowOffset / 2][c + 1].isEmpty()
                        && checkerMap[r + rowOffset / 2][c + 1].imageView.getTag() != tag
                        && tileMap[r + rowOffset][c + 2].isEmpty()) {
                    tileMarkToggle(tileMap[r + rowOffset][c + 2]);
                    checkEat(checkerMap[r + rowOffset][c + 2], false, tag, isTop ? 2 : 1);
                }
            }
        } else {
            if (c > 1 && r > 1 && state != 2
                    && !tileMap[r - 1][c - 1].isEmpty()
                    && checkerMap[r - 1][c - 1].imageView.getTag() != tag
                    && tileMap[r - 2][c - 2].isEmpty()) {
                if (state == -1)
                    tileMarkToggle(tileMap[r - 2][c - 2]);
                else
                    tilePotentialToggle(tileMap[r - 2][c - 2]);
                checkEat(checkerMap[r - 2][c - 2], false, tag, 4);
            }
            if (c > 1 && r < 6 && state != 1
                    && !tileMap[r + 1][c - 1].isEmpty()
                    && checkerMap[r + 1][c - 1].imageView.getTag() != tag
                    && tileMap[r + 2][c - 2].isEmpty()) {
                if (state == -1)
                    tileMarkToggle(tileMap[r + 2][c - 2]);
                else
                    tilePotentialToggle(tileMap[r + 2][c - 2]);
                checkEat(checkerMap[r + 2][c - 2], false, tag, 3);
            }
            if (c < 6 && r < 6 && state != 4
                    && !tileMap[r + 1][c + 1].isEmpty()
                    && checkerMap[r + 1][c + 1].imageView.getTag() != tag
                    && tileMap[r + 2][c + 2].isEmpty()) {
                if (state == -1)
                    tileMarkToggle(tileMap[r + 2][c + 2]);
                else
                    tilePotentialToggle(tileMap[r + 2][c + 2]);
                checkEat(checkerMap[r + 2][c + 2], false, tag, 2);
            }
            if (c < 6 && r > 1 && state != 3
                    && !tileMap[r - 1][c + 1].isEmpty()
                    && checkerMap[r - 1][c + 1].imageView.getTag() != tag
                    && tileMap[r - 2][c + 2].isEmpty()) {
                if (state == -1)
                    tileMarkToggle(tileMap[r - 2][c + 2]);
                else
                    tilePotentialToggle(tileMap[r - 2][c + 2]);
                checkEat(checkerMap[r - 2][c + 2], false, tag, 1);
            }
        }
    }

    /**
     * A function that toggle the marking and state of a tile.
     */
    private void tileMarkToggle(Tile tile) {
        tile.isMarked = !tile.isMarked;
        if (tile.isMarked)
            tile.imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        else
            tile.imageView.setColorFilter(Color.TRANSPARENT);
    }

    private void tilePotentialToggle(Tile tile) {
        tile.isPotential = !tile.isPotential;
        if (tile.isPotential)
            tile.imageView.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        else
            tile.imageView.setColorFilter(Color.TRANSPARENT);
    }

    public void clearUI() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tileMap[i][j].isMarked = false;
                tileMap[i][j].isPotential = false;
                tileMap[i][j].imageView.setColorFilter(Color.TRANSPARENT);
                checkerMap[i][j].isMarked = false;
                checkerMap[i][j].imageView.setColorFilter(Color.TRANSPARENT);
                if (checkerMap[i][j].imageView.getTag() == "p1")
                    checkerMap[i][j].imageView.setImageResource(skinP1);
                else if (checkerMap[i][j].imageView.getTag() == "p2")
                    checkerMap[i][j].imageView.setImageResource(skinP2);
            }
        }
    }

    public boolean checkForMarks() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tileMap[i][j].isMarked)
                    return true;
            }
        }
        return false;
    }
    private boolean checkForPotentials(Checker c) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tileMap[i][j].isPotential && Math.abs(c.row - i) == 2 && Math.abs(c.col - j) == 2)
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns the new Checker after refreshing the UI and moving the player if
     * more moves are possible (chain). Returns null if no more moves can be made.
     */
    public void moveToTile(Tile tile, Checker checker, Fragment game, boolean isUpdate) {
        Checker tileChecker = checkerMap[tile.getStander().row][tile.getStander().col];
        Object tempTag = tileChecker.imageView.getTag();
        boolean tempIsTop = tileChecker.isTop;

        // swap visibility
        tileChecker.imageView.setVisibility(View.VISIBLE);
        checker.imageView.setVisibility(View.INVISIBLE);

        // swap images
        tileChecker.imageView.setTag(checker.imageView.getTag());
        checker.imageView.setTag(tempTag);

        // swap isTop
        tileChecker.isTop = checker.isTop;
        checker.isTop = tempIsTop;

        // eat logic
        int deltaRow = tileChecker.row - checker.row;
        int deltaCol = tileChecker.col - checker.col;
        if (Math.abs(deltaRow) == 2 && Math.abs(deltaCol) == 2) {
            checkerMap[checker.row + deltaRow / 2]
                    [checker.col + deltaCol / 2]
                    .imageView.setVisibility(View.INVISIBLE);

            // vibrate logic
            Vibrator v = (Vibrator) game.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }

            // update score
            if (isMyTurn())
                ((GameFragment)game).scoreManager.eatScore();
        }

        if (!GameFragment.isSingle && !isUpdate) {
            isUpdating = true;
            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference moveRef = database.getReference(roomid).child("move");
            List<Integer> move = new ArrayList<Integer>();
            move.add(tileChecker.row);
            move.add(tileChecker.col);
            move.add(checker.row);
            move.add(checker.col);
            moveRef.setValue(move);
        }

        if (checkForPotentials(tileChecker)) { // check if potential on map
            clearUI();
            tileChecker.isMarked = true;
            tileChecker.imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            checkEat(tileChecker, false, tileChecker. imageView.getTag(), -1);
            if (checkForMarks()) {
                ((GameFragment) game).markedChecker = tileChecker;
                GameFragment.isMoving = true;
            }
        }
        // edge row have been reached, start bomb logic
        else if ((tileChecker.isTop && tileChecker.row == 7) || (!tileChecker.isTop && tileChecker.row == 0)) {
            GameFragment.isMoving = false;
            clearUI();
            tileChecker.imageView.setColorFilter(Color.GREEN);
            ((GameFragment)game).bombChecker = isUpdate ? null : tileChecker;
            ((GameFragment)game).markedChecker = null;
        }
        else {
            GameFragment.isMoving = false;
            clearUI();
            if (!isUpdate)
                switchTurn(GameFragment.isSingle, (GameFragment) game);
            ((GameFragment)game).markedChecker = null;
        }

        // move sound effect
        if (turnOwner == 1)
            MediaPlayer.create(game.getContext(), R.raw.pop1).start();
        else
            MediaPlayer.create(game.getContext(), R.raw.pop2).start();
    }

    /**
     * A function that checks if the game has ended.
     * @return the number of the winning player, 3 for a draw and 0 if the game has not yet ended.
     */
    public int endState() {
        int p1Count = 0;
        int p2Count = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ImageView iv = checkerMap[i][j].imageView;

                if (iv.getTag().toString().contains("boom"))
                    continue;

                if (iv.getTag().equals("p1") && iv.getVisibility() == View.VISIBLE)
                    p1Count++;
                else if (iv.getTag().equals("p2") && iv.getVisibility() == View.VISIBLE)
                    p2Count++;
            }
        }

        if (p1Count == 0 && p2Count == 0)
            return 3;
        else if (p1Count == 0)
            return 2;
        else if (p2Count == 0)
            return 1;
        return 0;
    }

    public boolean hasMoves(GameFragment game) {
        tileManager.clearUI();

        ArrayList<Checker> availableCheckers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Checker c = checkerMap[i][j];
                if (c.imageView.getVisibility() == View.VISIBLE
                        && c.imageView.getTag().equals(turnOwner == 1 ? "p1" : "p2"))
                    availableCheckers.add(c);
            }
        }
        game.isChecking = true;
        for (Checker c : availableCheckers) {
            displayMoveHelper(c);
            if (checkForMarks()) {
                clearUI();
                game.isChecking = false;
                return true;
            }
        }
        game.isChecking = false;
        return false;
    }
}
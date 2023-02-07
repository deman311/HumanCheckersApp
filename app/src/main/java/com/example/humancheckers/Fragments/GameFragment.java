package com.example.humancheckers.Fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.humancheckers.BotController;
import com.example.humancheckers.Models.Checker;
import com.example.humancheckers.Models.Player;
import com.example.humancheckers.Models.Tile;
import com.example.humancheckers.R;
import com.example.humancheckers.Managers.ScoreManager;
import com.example.humancheckers.Managers.TileManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameFragment extends Fragment {

    public static int skinP1 = -1, skinP2 = -1;
    public static int tile1 = -1, tile2 = -1;
    public static boolean isSingle = true;
    public static TileManager tileManager;
    public static String username1, username2;
    public static boolean isMoving, hasEnded;

    public BotController bot = null;
    public Checker markedChecker, bombChecker;
    public ScoreManager scoreManager;
    public boolean isChecking = false;

    // set debug players
    public Player p1 = new Player().setName("Player1").setSkin(R.drawable.chk_warrior);
    public Player p2 = new Player().setName("Player2").setSkin(R.drawable.chk_archer);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        isMoving = false;
        hasEnded = false;

        if (!isSingle) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("rooms").document(getActivity().getIntent().getStringExtra("roomID")).get().addOnSuccessListener(sl -> {
                // set values from DB
                skinP1 = Integer.parseInt(sl.getString("skinP1"));
                tile1 = Integer.parseInt(sl.getString("tileP1"));
                skinP2 = Integer.parseInt(sl.getString("skinP2"));
                tile2 = Integer.parseInt(sl.getString("tileP2"));
                username1 = sl.getString("user1");
                username2 = sl.getString("user2");

                initFragment();
            });
        }

        return view;
    }

    public void initFragment() {
        // define default player skins if not defined for some reason
        // else set the name and skin defined in the db that has been passed in the GameActivity
        if (skinP1 == -1) skinP1 = p1.getSkin();
        else {
            p1.setName(username1);
            p1.setSkin(skinP1);
        }
        if (skinP2 == -1) skinP2 = p2.getSkin();
        else {
            p2.setName(username2);
            p2.setSkin(skinP2);
        }

        // create the tile manager
        tileManager = new TileManager(skinP1, skinP2);
        tileManager.player = getActivity().getIntent().getIntExtra("pNum", 0);;
        isMoving = false;
        markedChecker = null;

        // create a bot in case needed
        if (isSingle)
            bot = new BotController(tileManager);

        // create tile table layout
        TableLayout tileLayout = getActivity().findViewById(R.id.game_TBL_tileset);
        for (int i = 0; i < 8; i++) {
            tileLayout.setColumnShrinkable(i, true);
            tileLayout.setStretchAllColumns(true);
            TableRow tableRow = new TableRow(getContext());
            tableRow.setWeightSum(1);
            for (int j = 0; j < 8; j++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
                if ((i + j) % 2 == 0) {
                    imageView.setImageResource(tile1 != -1 ? tile1 : R.drawable.tile_grass);
                } else {
                    imageView.setImageResource(tile2 != -1 ? tile2 : R.drawable.tile_ground);
                }
                tableRow.addView(imageView);
                tileManager.setTile(i, j, imageView);

                // set move logic
                final int fi = i, fj = j;
                imageView.setOnClickListener(cl -> {
                    Tile tile = tileManager.tileMap[fi][fj];
                    if (tile.isMarked && markedChecker != null) {
                        if (tileManager.isMyTurn()) // only update the score for my turns.
                            scoreManager.moveScore();
                        tileManager.moveToTile(tile, markedChecker, this, false);

                        if (markedChecker == null && bombChecker == null) {
                            updatePlayerPrompt();

                            // bot logic
                            if (isSingle && tileManager.turnOwner == 2) {
                                new Handler().postDelayed(() -> bot.makeTurn(), 500);
                                updatePlayerPrompt();
                            }
                        } else if (bombChecker != null)
                            bombPrompt();
                    }

                    // check end state
                    if (!hasEnded)
                        checkEndGame();
                });
            }
            tileLayout.addView(tableRow);
        }

        // create checker table layout
        TableLayout checkerLayout = getActivity().findViewById(R.id.game_TBL_checkerset);
        for (int i = 0; i < 8; i++) {
            checkerLayout.setColumnShrinkable(i, true);
            TableRow tableRow = new TableRow(getContext());
            for (int j = 0; j < 8; j++) {
                boolean isTop;
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
                if (i < 4) {
                    isTop = true;
                    imageView.setImageResource(skinP1);
                    imageView.setTag("p1");
                } else {
                    isTop = false;
                    imageView.setImageResource(skinP2);
                    imageView.setTag("p2");
                }
                tableRow.addView(imageView);

                // define click logic
                final int fi = i, fj = j;
                imageView.setOnClickListener(cl -> {
                    if (isMoving    // cannot move while chaining
                            || (!isSingle && !tileManager.isMyTurn())    // multiplayer click
                            // cannot click tiles that are not mine
                            || (cl.getTag().equals("p1") && tileManager.turnOwner == 2 && bombChecker == null)
                            || (cl.getTag().equals("p2") && tileManager.turnOwner == 1 && bombChecker == null))
                        return;
                    else if (bombChecker != null) {
                        bombChecker.imageView.setVisibility(View.INVISIBLE);
                        bombSoundAndAnimation(fi, fj);

                        if (!isSingle)
                            updateBomb(bombChecker.row, bombChecker.col, fi, fj);
                        else
                            tileManager.switchTurn(true, this);

                        updatePlayerPrompt();
                        bombChecker = null;

                        // update score
                        if (!tileManager.isMyTurn()) // turn is switched beforehand when onMove ends.
                            scoreManager.bombScore();

                        // check end state
                        checkEndGame();

                        // deal with bot click logic
                        // turn ended thus pass to the bot
                        if (!hasEnded && !isChecking && isSingle && tileManager.turnOwner == 2) {
                            new Handler().postDelayed(() -> bot.makeTurn(), 500);
                        }
                        return;
                    }
                    if (hasEnded)
                        return;

                    Checker checker = tileManager.checkerMap[fi][fj];
                    if (!checker.isMarked) {
                        if (markedChecker != null)
                            tileManager.clearUI();
                        markedChecker = checker;
                        tileManager.displayMoveHelper(markedChecker);
                        checker.toggleMark();
                    } else {
                        checker.toggleMark();
                        tileManager.clearUI();
                        markedChecker = null;
                    }
                });
                tileManager.setChecker(i, j, imageView, isTop);

                if (i > 2 && i < 5 || (j + i) % 2 == 1)
                    imageView.setVisibility(View.INVISIBLE);
                else
                    imageView.setVisibility(View.VISIBLE);
                tileManager.tileMap[i][j].setStander(tileManager.checkerMap[i][j]);
            }
            checkerLayout.addView(tableRow);
        }

        // actions that are supposed to happen only after the fragments have been initialized.
        updatePlayerPrompt();
        scoreManager =
                ((BoardFragment) getParentFragmentManager().findFragmentById(R.id.board_fragment)).getScoreManager();
        if (isSingle)
            checkBotTurn();
    }

    public void bombSoundAndAnimation(int fi, int fj) {
        // play sound
        MediaPlayer.create(getActivity(), R.raw.game_bomb).start();

        // animation, stop after 1 iteration, play with a green filter
        // add 'boom' string to tag so that the CheckEndState won't count the tile while the thread has not finished running (race condition fix)
        tileManager.checkerMap[fi][fj].imageView.setTag("boom" + tileManager.checkerMap[fi][fj].imageView.getTag().toString());
        Glide.with(getView()).asGif()
                .load(R.drawable.boom)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .skipMemoryCache(true)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        resource.setLoopCount(1);
                        tileManager.checkerMap[fi][fj].imageView.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                        return false;
                    }
                })
                .into(tileManager.checkerMap[fi][fj].imageView);
        ViewPropertyAnimator anim = tileManager.checkerMap[fi][fj].imageView.animate();
        anim.setDuration(500).withEndAction(() -> {
            tileManager.checkerMap[fi][fj].imageView.setVisibility(View.INVISIBLE);
            tileManager.checkerMap[fi][fj].imageView.setTag(tileManager.checkerMap[fi][fj].imageView.getTag().toString().replace("boom", ""));
        }).start();
    }

    public void updateMove(int fromRow, int fromCol, int toRow, int toCol) {
        Tile t = tileManager.tileMap[fromRow][fromCol];
        Checker c = tileManager.checkerMap[toRow][toCol];
        tileManager.moveToTile(t, c, this, true);
        checkEndGame();
    }

    public void updateBomb(int row1, int col1, int row2, int col2) {
        GameFragment.tileManager.isUpdating = true;
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://androidcoursefinalproject-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference bombRef = database.getReference(tileManager.roomid).child("bomb");
        List<Integer> data = new ArrayList<>();
        data.add(row1);
        data.add(col1);
        data.add(row2);
        data.add(col2);
        bombRef.setValue(data);
        tileManager.switchTurn(isSingle, this);
    }

    public void updatePlayerPrompt() {
        String currentPlayer = tileManager.turnOwner == 1 ? p1.getName() : p2.getName();
        ((BoardFragment)
                getParentFragmentManager().findFragmentById(R.id.board_fragment))
                .playerPrompt(currentPlayer);
    }

    public void bombPrompt() {
        ((BoardFragment)
                getParentFragmentManager().findFragmentById(R.id.board_fragment))
                .bombPrompt();
    }

    public void promptStuck() {
        ((BoardFragment)
                getParentFragmentManager().findFragmentById(R.id.board_fragment))
                .stuckPrompt();
    }

    public void checkEndGame() {
        int endState = tileManager.endState();
        if (endState == 0) {
            hasEnded = false;
            return;
        }
        else
            hasEnded = true;

        if ((isSingle && endState == 1) || endState == 3 || tileManager.player == endState)
            MediaPlayer.create(getActivity(), R.raw.win).start();
        else
            MediaPlayer.create(getActivity(), R.raw.lose).start();

        BoardFragment boardFragment = ((BoardFragment)
                getParentFragmentManager().findFragmentById(R.id.board_fragment));
        if (endState == 3)
            boardFragment.drawPrompt();
        else
            boardFragment.winPrompt(endState == 1 ? p1.getName() : p2.getName());

        tileManager.clearUI();
        tileManager.player = 0;
        tileManager.turnOwner = 0;
        AnimateWin(true);

        // upload to the database if highscore is beat
        if (!isSingle) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("highscores")
                    .orderBy("score", Query.Direction.DESCENDING).get().addOnSuccessListener(sl -> {
                        int myScore = scoreManager.getScore();
                        for (DocumentSnapshot doc : sl.getDocuments()) {
                            int score = doc.get("score", Integer.class);
                            if (myScore > score) {
                                if (sl.getDocuments().size() == 10)
                                    doc.getReference().delete();
                                Map<String, Object> data = new HashMap<>();
                                data.put("score", myScore);
                                data.put("date", FieldValue.serverTimestamp());
                                db.collection("highscores")
                                        .document(GameFragment.tileManager.player == 1 ? username1 : username2)
                                        .set(data, SetOptions.merge());
                                break;
                            }
                        }
                    });
        }
    }

    public void AnimateWin(boolean isFirst) {
        Random rand = new Random();
        for (int i = 0; i < (isFirst ? 5 : 1); i++) {
            ImageView iv = tileManager.tileMap[rand.nextInt(8)]
                    [rand.nextInt(8)].imageView;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // set to random color
                iv.setColorFilter(Color.rgb(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            }
            ViewPropertyAnimator anim = iv.animate();
            anim.scaleY(1.5f).scaleX(1.5f).setDuration(500).withEndAction(() -> {
                anim.scaleX(1).scaleY(1).withEndAction(() -> {
                    anim.cancel();
                    iv.setColorFilter(Color.TRANSPARENT);
                    AnimateWin(false);
                }).start();
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tileManager.tileMap[i][j].imageView.animate().cancel();
                tileManager.tileMap[i][j].imageView.setColorFilter(Color.TRANSPARENT);
            }
        }
    }

    public void checkBotTurn() {
        if (tileManager.turnOwner == 2)
            new Handler().postDelayed(() -> bot.makeTurn(), 500);
    }

    public static void setTurnOwner(int val) {
        tileManager.turnOwner = val;
    }
}
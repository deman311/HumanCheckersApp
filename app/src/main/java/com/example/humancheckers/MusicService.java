package com.example.humancheckers;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service {
    private MediaPlayer player;
    private Intent intent;
    private int flags;
    private int startId;

    private ArrayList<Integer> songs = new ArrayList<Integer>();
    private int songIndex;

    @Override
    public void onCreate() {
        super.onCreate();
        initList();
        songIndex = new Random().nextInt(songs.size());
        player = MediaPlayer.create(this, songs.get(songIndex));   // start a random song
        player.setOnCompletionListener(cl -> {
            songIndex++;
            if (songIndex == songs.size())
                songIndex = 0;
            player = MediaPlayer.create(this, songs.get(songIndex));
            player.start();
        });
        player.setVolume(0.5f, 0.5f);
    }

    private void initList() {
        songs.add(R.raw.ms1);
        songs.add(R.raw.ms2);
        songs.add(R.raw.ms3);
        songs.add(R.raw.ms4);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        this.flags = flags;
        this.startId = startId;
        player.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

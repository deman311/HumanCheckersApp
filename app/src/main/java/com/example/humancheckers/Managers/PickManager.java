package com.example.humancheckers.Managers;

import com.example.humancheckers.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PickManager {
    private ArrayList<Integer> skins;
    private Map<Integer, String> skinToName;

    private ArrayList<Integer> tiles;
    private Map<Integer, String> tileToName;

    private int currentIndex = 0;

    public PickManager() {
        initCharacterSkins();
        initTileSkins();
    }

    public int getRandomTile() {
        return tiles.get(new Random().nextInt(tiles.size()));
    }
    public int getRandomSkin() {
        return skins.get(new Random().nextInt(skins.size()));
    }

    public int leftTile() {
        currentIndex--;
        if (currentIndex < 0)
            currentIndex = tiles.size() - 1;
        return tiles.get(currentIndex);
    }
    public int rightTile() {
        currentIndex++;
        if (currentIndex == tiles.size())
            currentIndex = 0;
        return tiles.get(currentIndex);
    }
    public int currentTile() {
        return tiles.get(currentIndex);
    }
    public int chooseTile() {
        int tile = tiles.remove(currentIndex);
        currentIndex = 0; // reset index after choosing a skin.
        return tile;
    }
    public String currentTileName() {
        return tileToName.get(tiles.get(currentIndex));
    }

    public int leftSkin() {
        currentIndex--;
        if (currentIndex < 0)
            currentIndex = skins.size() - 1;
        return skins.get(currentIndex);
    }
    public int rightSkin() {
        currentIndex++;
        if (currentIndex == skins.size())
            currentIndex = 0;
        return skins.get(currentIndex);
    }
    public int currentSkin() {
        return skins.get(currentIndex);
    }
    public int chooseSkin() {
        int skin = skins.remove(currentIndex);
        currentIndex = 0; // reset index after choosing a skin.
        return skin;
    }
    public String currentSkinName() {
        return skinToName.get(skins.get(currentIndex));
    }

    private void initCharacterSkins() {
        skins = new ArrayList<>();
        skinToName = new HashMap<>();

        skins.add(R.drawable.chk_warrior);
        skinToName.put(R.drawable.chk_warrior, "The Warrior");
        skins.add(R.drawable.chk_archer);
        skinToName.put(R.drawable.chk_archer, "The Archer");
        skins.add(R.drawable.chk_wizard);
        skinToName.put(R.drawable.chk_wizard, "The Wizard");
        skins.add(R.drawable.chk_monster);
        skinToName.put(R.drawable.chk_monster, "The Monster");
        skins.add(R.drawable.chk_grim);
        skinToName.put(R.drawable.chk_grim, "The Reaper");
        skins.add(R.drawable.chk_ogre);
        skinToName.put(R.drawable.chk_ogre, "The Ogre");
    }
    private void initTileSkins() {
        tiles = new ArrayList<>();
        tileToName = new HashMap<>();

        tiles.add(R.drawable.tile_grass);
        tileToName.put(R.drawable.tile_grass, "Grass");
        tiles.add(R.drawable.tile_ground);
        tileToName.put(R.drawable.tile_ground, "Ground");
        tiles.add(R.drawable.tile_rock);
        tileToName.put(R.drawable.tile_rock, "Rock");
        tiles.add(R.drawable.tile_water);
        tileToName.put(R.drawable.tile_water, "Water");
    }
}

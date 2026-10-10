package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AgeOfMayaneGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;

    @Override
    public void create() {
        Gdx.app.log("AGE_OF_MAYANE", "=== CREATION DU JEU ===");
        try {
            batch = new SpriteBatch();
            Gdx.app.log("AGE_OF_MAYANE", "SpriteBatch OK");

            font = new BitmapFont();
            Gdx.app.log("AGE_OF_MAYANE", "BitmapFont OK");

            font.getData().setScale(2f);
            Gdx.app.log("AGE_OF_MAYANE", "Font scale OK");

            Gdx.app.log("AGE_OF_MAYANE", "=== CREATION GAMESCREEN ===");
            setScreen(new GameScreen(this));
            Gdx.app.log("AGE_OF_MAYANE", "=== GAMESCREEN OK ===");
        } catch (Throwable t) {
            Gdx.app.error("AGE_OF_MAYANE", "ERREUR CREATE: " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        super.dispose();
    }
}
package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AgeOfMayaneGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        Gdx.app.log("AGE_OF_MAYANE", "=== CREATE START ===");
        try {
            batch = new SpriteBatch();
            Gdx.app.log("AGE_OF_MAYANE", "SpriteBatch OK");
            setScreen(new GameScreen(this));
            Gdx.app.log("AGE_OF_MAYANE", "=== CREATE DONE ===");
        } catch (Throwable t) {
            Gdx.app.error("AGE_OF_MAYANE", "ERREUR: " + t.getClass().getName() + " - " + t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        super.dispose();
    }
}
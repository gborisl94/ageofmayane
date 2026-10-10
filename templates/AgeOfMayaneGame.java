package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AgeOfMayaneGame extends Game {
    public SpriteBatch batch;
    public String lastError = "";
    public static AgeOfMayaneGame INSTANCE;

    @Override
    public void create() {
        INSTANCE = this;
        try {
            batch = new SpriteBatch();
            setScreen(new GameScreen(this));
        } catch (Throwable t) {
            lastError = "CREATE ERREUR:\n" + t.getClass().getSimpleName() + ": " + t.getMessage();
            StringBuilder sb = new StringBuilder(lastError + "\n\nSTACK:\n");
            for (StackTraceElement e : t.getStackTrace()) {
                sb.append("  ").append(e.toString()).append("\n");
                if (sb.length() > 3000) break;
            }
            lastError = sb.toString();
            Gdx.app.error("AGE_OF_MAYANE", lastError);
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        super.dispose();
    }
}
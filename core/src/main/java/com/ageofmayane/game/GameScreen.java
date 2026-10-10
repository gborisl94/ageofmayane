package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

public class GameScreen extends ScreenAdapter {
    private final AgeOfMayaneGame game;

    public GameScreen(AgeOfMayaneGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.app.log("AGE_OF_MAYANE", "GameScreen.show() OK");
    }

    @Override
    public void render(float delta) {
        // Écran rouge simple — rien d'autre
        Gdx.gl.glClearColor(0.3f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        // rien
    }
}
package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private final AgeOfMayaneGame game;
    private OrthographicCamera camera;
    private ShapeRenderer sr;

    private float px, py;
    private final float PRADIUS = 22f;

    public GameScreen(AgeOfMayaneGame game) { this.game = game; }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_W, WORLD_H);
        px = WORLD_W / 2f;
        py = WORLD_H / 2f;
    }

    @Override
    public void render(float delta) {
        // Input
        float speed = 320f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) py += speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) py -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) px -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) px += speed * delta;

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Grille de fond
        sr.setColor(0.12f, 0.12f, 0.18f, 1f);
        for (float x = 0; x <= WORLD_W; x += 80f) sr.rectLine(x, 0, x, WORLD_H, 1f);
        for (float y = 0; y <= WORLD_H; y += 80f) sr.rectLine(0, y, WORLD_W, y, 1f);

        // Player vert
        sr.setColor(0.2f, 0.9f, 0.4f, 1f);
        sr.circle(px, py, PRADIUS, 32);

        sr.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, WORLD_W, WORLD_H);
    }

    @Override
    public void dispose() {
        if (sr != null) sr.dispose();
    }
}
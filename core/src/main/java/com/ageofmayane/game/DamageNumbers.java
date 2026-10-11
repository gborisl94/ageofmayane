package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

/** Nombres de degats flottants (« -1 »). */
class DamageNumbers {

    static final float LIFETIME = 0.8f;
    static final float RISE_SPEED = 60f;
    static final float SCALE = 1.2f;      // la police de base est a 2.0
    static final int MAX_NUMBERS = 40;

    private static class Item {
        float x;
        float y;
        float life;
        String text;
        boolean player;
    }

    private final Array<Item> items = new Array<Item>();
    private final GlyphLayout layout = new GlyphLayout();

    /** player = true : degats subis par le joueur (rouge). */
    void add(float x, float y, String text, boolean player) {
        if (items.size >= MAX_NUMBERS) items.removeIndex(0);
        Item it = new Item();
        it.x = x;
        it.y = y;
        it.text = text;
        it.player = player;
        it.life = LIFETIME;
        items.add(it);
    }

    void update(float delta) {
        for (int i = items.size - 1; i >= 0; i--) {
            Item it = items.get(i);
            it.life -= delta;
            it.y += RISE_SPEED * delta;
            if (it.life <= 0f) items.removeIndex(i);
        }
    }

    void clear() {
        items.clear();
    }

    /** A appeler entre batch.begin() et batch.end(), avec la camera du monde. */
    void draw(SpriteBatch batch, BitmapFont font) {
        if (items.size == 0) return;
        float oldX = font.getData().scaleX;
        float oldY = font.getData().scaleY;
        font.getData().setScale(SCALE);

        for (int i = 0; i < items.size; i++) {
            Item it = items.get(i);
            float a = Math.min(1f, it.life / (LIFETIME * 0.5f));
            if (it.player) font.setColor(1f, 0.25f, 0.25f, a);
            else font.setColor(1f, 0.9f, 0.3f, a);
            layout.setText(font, it.text);
            font.draw(batch, it.text, it.x - layout.width / 2f, it.y);
        }

        font.getData().setScale(oldX, oldY);
        font.setColor(Color.WHITE);
    }
}

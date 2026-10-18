 com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class GameFloor {

    static void nextFloor(GameScreen gs) {
        if (gs.bossFledThisRun) gs.vengeance.registerFlee();
        gs.vengeance.consumePending();
        gs.currentFloor++;
        gs.xpSystem.addXp(50 + (gs.currentFloor * 10)); // Plus on monte, plus on gagne d'XP
        gs.floorBannerTimer = 3f;
        if (gs.currentFloor > GameScreen.TOTAL_FLOORS) {
            gs.victory = true;
            gs.endTimer = 0f;
            return;
        }
        gs.dungeon.generate();
        gs.playerPos.set(GameScreen.W / 2f, GameScreen.H / 2f);
        //ne accolade ferme probablement la méthode

    if (gs.currentFloor == 100) {
        gs.triggerShake();
}
        gs.fleches.clear();
        gs.ennemis.clear();
        gs.weaponDrops.clear();
        gs.damage.clear();
        gs.particles.clear();
        gs.moveStick.end();
        gs.aimStick.end();
        for (int i = 
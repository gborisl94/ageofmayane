package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;

class GameInput {

    static void pollTouches(GameScreen gs) {
        gs.zoom.resetFrame();
        gs.newTouch = false;
        for (int i = 0; i < GameScreen.MAX_POINTERS; i++) {
            if (!Gdx.input.isTouched(i)) {
                if (gs.moveStick.pointer == i) gs.moveStick.end();
                if (gs.aimStick.pointer == i) gs.aimStick.end();
                gs.owner[i] = GameScreen.OWNER_NONE;
                if (x >= GameScreen.ARC_BTN_X
        && x <= GameScreen.ARC_BTN_X + GameScreen.ARC_BTN_SIZE
        && y >= GameScreen.ARC_BTN_Y
        && y <= GameScreen.ARC_BTN_Y + GameScreen.ARC_BTN_SIZE) {
    gs.arcBtnActive = true;
    gs.arcBtnTimer = 0f;
    gs.arcBtnPointer = i;
    gs.newTouch = true;
    gs.owner[i] = GameScreen.OWNER_IGNORED;
    continue;
                }
                continue;
            }
            gs.tmp.set(Gdx.input.getX(i), Gdx.input.getY(i));
            gs.renderer.uiViewport.unproject(gs.tmp);
            float x = gs.tmp.x;
            float y = gs.tmp.y;

            if (gs.owner[i] == GameScreen.OWNER_NONE) {
                if (gs.elevatorButton != null && gs.elevatorButton.isVisible()
                        && gs.elevatorButton.getBounds().contains(x, y)) {
                    if (gs.elevatorButton.handleClick(x, y)) {
                        gs.tryUseElevator();
                        gs.newTouch = true;
                        gs.owner[i] = GameScreen.OWNER_IGNORED;
                        continue;
                    }
                }

                if (x >= GameScreen.AIM_BTN_X && x <= GameScreen.AIM_BTN_X + GameScreen.AIM_BTN_SIZE
                        && y >= GameScreen.AIM_BTN_Y && y <= GameScreen.AIM_BTN_Y + GameScreen.AIM_BTN_SIZE) {
                    if (gs.autoAimActive) gs.deactivateAutoAimPower();
                    else if (gs.isAutoAimReady()) gs.activateAutoAimPower();
                    gs.newTouch = true;
                    gs.owner[i] = GameScreen.OWNER_IGNORED;
                } else if (x >= GameScreen.STUN_BTN_X && x <= GameScreen.STUN_BTN_X + GameScreen.STUN_BTN_SIZE
                        && y >= GameScreen.STUN_BTN_Y && y <= GameScreen.STUN_BTN_Y + GameScreen.STUN_BTN_SIZE) {
                    if (gs.isStunReady()) gs.activateStunAbility();
                    gs.newTouch = true;
                    gs.owner[i] = GameScreen.OWNER_IGNORED;
                } else if (x >= GameScreen.GLITCH_BTN_X && x <= GameScreen.GLITCH_BTN_X + GameScreen.GLITCH_BTN_SIZE
                        && y >= GameScreen.GLITCH_BTN_Y && y <= GameScreen.GLITCH_BTN_Y + GameScreen.GLITCH_BTN_SIZE) {
                    if (gs.glitchCooldown <= 0f) gs.activateGlitchDash();
                    gs.newTouch = true;
                    gs.owner[i] = GameScreen.OWNER_IGNORED;
                } else if (gs.canInteractWithChest() && x >= GameScreen.OPEN_BTN_X
                        && x <= GameScreen.OPEN_BTN_X + GameScreen.OPEN_BTN_W
                        && y >= GameScreen.OPEN_BTN_Y && y <= GameScreen.OPEN_BTN_Y + GameScreen.OPEN_BTN_H) {
                    gs.openChest();
                    gs.newTouch = true;
                    gs.owner[i] = GameScreen.OWNER_IGNORED;
                } else if (gs.zoom.handleTouch(x, y)) {
                    gs.owner[i] = GameScreen.OWNER_ZOOM;
                } else {
                    gs.newTouch = true;
                    if (x < GameScreen.W / 2f && !gs.moveStick.active()) {
                        gs.owner[i] = GameScreen.OWNER_MOVE;
                        gs.moveStick.begin(i, x, y);
                    } else if (x >= GameScreen.W / 2f && !gs.aimStick.active()) {
                        gs.owner[i] = GameScreen.OWNER_AIM;
                        gs.aimStick.begin(i, x, y);
                    } else {
                        gs.owner[i] = GameScreen.OWNER_IGNORED;
                    }
                }
            } else if (gs.owner[i] == GameScreen.OWNER_ZOOM) {
                gs.zoom.handleTouch(x, y);
            } else if (gs.owner[i] == GameScreen.OWNER_MOVE && gs.moveStick.pointer == i) {
                gs.moveStick.move(x, y);
            } else if (gs.owner[i] == GameScreen.OWNER_AIM && gs.aimStick.pointer == i) {
                gs.aimStick.move(x, y);
            }
        }
    }
}
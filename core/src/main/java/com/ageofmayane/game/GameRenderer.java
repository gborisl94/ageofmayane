/** Bouton du pouvoir AUTO-AIM : 3 etats (PRET / ACTIF / RECHARGE). */
private void drawAutoAimButton(GameScreen gs) {
    float bx = GameScreen.AIM_BTN_X;
    float by = GameScreen.AIM_BTN_Y;
    float bs = GameScreen.AIM_BTN_SIZE;

    boolean ready = gs.isAutoAimReady();
    boolean active = gs.autoAimActive;

    shapes.begin(ShapeRenderer.ShapeType.Filled);

    // Cadre noir
    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 3f, by - 3f, bs + 6f, bs + 6f);

    // Fond selon l'etat
    if (active) {
        // OR lumineux
        shapes.setColor(1f, 0.85f, 0.2f, 0.98f);
    } else if (ready) {
        // Vert
        shapes.setColor(0.2f, 0.75f, 0.35f, 0.95f);
    } else {
        // Gris pendant recharge
        shapes.setColor(0.3f, 0.3f, 0.35f, 0.9f);
    }
    shapes.rect(bx, by, bs, bs);

    // Barre de progression :
    //  - ACTIF : duree restante (decremente)
    //  - RECHARGE : cooldown restant (decremente)
    if (active) {
        float ratio = gs.getAutoAimDurationRatio();
        // barre qui se vide par la gauche
        shapes.setColor(0.6f, 0.4f, 0.05f, 0.7f);
        shapes.rect(bx, by, bs * ratio, 8f);
    } else if (!ready) {
        float ratio = gs.getAutoAimCooldownRatio();
        // barre qui se remplit par le bas
        shapes.setColor(0.15f, 0.15f, 0.2f, 0.75f);
        float barH = bs * ratio;
        shapes.rect(bx, by, bs, barH);
    }

    // Symbole de cible au centre
    float cx = bx + bs / 2f;
    float cy = by + bs / 2f + 5f;

    if (active) {
        // Cible lumineuse
        shapes.setColor(1f, 1f, 1f, 0.95f);
    } else if (ready) {
        shapes.setColor(1f, 1f, 1f, 0.9f);
    } else {
        shapes.setColor(0.7f, 0.7f, 0.7f, 0.7f);
    }

    float r1 = 22f;
    float r2 = 12f;
    int segments = 16;
    for (int i = 0; i < segments; i++) {
        float a = i * (360f / segments);
        float px1 = cx + MathUtils.cosDeg(a) * r1;
        float py1 = cy + MathUtils.sinDeg(a) * r1;
        shapes.rect(px1 - 1.5f, py1 - 1.5f, 3f, 3f);

        float px2 = cx + MathUtils.cosDeg(a) * r2;
        float py2 = cy + MathUtils.sinDeg(a) * r2;
        shapes.rect(px2 - 1f, py2 - 1f, 2f, 2f);
    }
    shapes.rect(cx - 2f, cy - 2f, 4f, 4f);

    // Si en recharge, on barre la cible
    if (!ready && !active) {
        shapes.setColor(1f, 0.2f, 0.2f, 0.75f);
        shapes.rectLine(cx - 26f, cy - 26f, cx + 26f, cy + 26f, 5f);
    }

    shapes.end();

    // ==================== TEXTE ====================
    batch.begin();

    // Ligne 1 : nom court
    font.getData().setScale(1.3f);
    String txt;
    Color txtColor;
    if (active) {
        txt = "ACTIF";
        txtColor = new Color(0.2f, 0.1f, 0f, 1f);
    } else if (ready) {
        txt = "PRET";
        txtColor = new Color(1f, 1f, 1f, 1f);
    } else {
        txt = "RECHARGE";
        txtColor = new Color(1f, 0.7f, 0.7f, 1f);
    }
    layout.setText(font, txt);
    float tx = bx + (bs - layout.width) / 2f;
    float ty = by + 22f;

    font.setColor(0f, 0f, 0f, 0.8f);
    font.draw(batch, txt, tx + 1f, ty + 1f);
    font.setColor(txtColor);
    font.draw(batch, txt, tx, ty);

    // Ligne 2 : compteur
    font.getData().setScale(1.5f);
    String sub;
    if (active) {
        sub = String.format("%.1fs", gs.autoAimTimer);
    } else if (ready) {
        sub = "AIM";
    } else {
        sub = String.format("%.1fs", gs.autoAimCooldown);
    }
    layout.setText(font, sub);
    font.setColor(1f, 1f, 1f, 0.95f);
    font.draw(batch, sub, bx + (bs - layout.width) / 2f, by + bs - 10f);

    font.getData().setScale(2f);
    font.setColor(Color.WHITE);
    batch.end();
}
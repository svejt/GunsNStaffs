package io.github.roguelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Boss health bar, pripet na dno zaslona (vodoravno na sredini).
 *
 * Življenjski cikel: show(...) ob začetku boja (bar se postopoma prikaže), setHealth(...) vsak frame,
 * hide() ko boss pade (bar se postopoma skrije). update(delta) poskrbi za animacije, render(...) riše.
 * Ob zadetku rumen "trail" počasi dohiti dejansko HP, da je izgubljeni HP lepo viden.
 */
public class BossHealthBar implements Disposable {
    private static final float BAR_WIDTH = 320f;
    private static final float BAR_HEIGHT = 22f;
    private static final float TEXT_PADDING = 6f;
    private static final float FADE_SPEED = 3f;        // alpha na sekundo
    private static final float TRAIL_DELAY = 0.4f;     // sekunde, preden trail začne pojemati
    private static final float TRAIL_SPEED = 0.5f;     // delež maxHp na sekundo

    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private String name = "";
    private float maxHp = 1f;
    private float hp = 1f;
    private float trailHp = 1f;
    private float trailDelay;
    private float alpha;
    private boolean active;

    public BossHealthBar() {
        font.getData().setScale(0.8f);
    }

    /** Začne boj: bar se pojavi s polnim HP. */
    public void show(String name, float maxHp) {
        this.name = name;
        this.maxHp = Math.max(1f, maxHp);
        this.hp = this.maxHp;
        this.trailHp = this.maxHp;
        this.trailDelay = 0f;
        this.alpha = 0f;
        this.active = true;
    }

    /** Skrije bar (npr. ko boss pade); izgine postopoma. */
    public void hide() {
        active = false;
    }

    /** Nastavi trenutni HP bossa. Padec HP-ja sproži trail animacijo. */
    public void setHealth(float currentHp) {
        float newHp = MathUtils.clamp(currentHp, 0f, maxHp);
        if (newHp < hp) trailDelay = TRAIL_DELAY;
        hp = newHp;
    }

    public boolean isVisible() {
        return alpha > 0f;
    }

    public void update(float delta) {
        if (active) alpha = Math.min(1f, alpha + FADE_SPEED * delta);
        else alpha = Math.max(0f, alpha - FADE_SPEED * delta);

        if (trailHp < hp) {
            trailHp = hp;
        } else if (trailHp > hp) {
            trailDelay -= delta;
            if (trailDelay <= 0f) trailHp = Math.max(hp, trailHp - maxHp * TRAIL_SPEED * delta);
        }
    }

    /**
     * Nariše bar. y je spodnji rob bara v svetovnih koordinatah, projection pa matrika
     * viewporta (isti koordinatni sistem kot ostali HUD).
     */
    public void render(ShapeRenderer shapes, SpriteBatch batch, Matrix4 projection, float worldW, float y) {
        if (alpha <= 0f) return;

        float x = (worldW - BAR_WIDTH) / 2f;
        float hpWidth = BAR_WIDTH * (hp / maxHp);
        float trailWidth = BAR_WIDTH * (trailHp / maxHp);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.04f, 0.1f, 0.85f * alpha);   // ozadje
        shapes.rect(x, y, BAR_WIDTH, BAR_HEIGHT);
        shapes.setColor(0.95f, 0.8f, 0.25f, alpha);           // trail (izgubljeni HP)
        shapes.rect(x, y, trailWidth, BAR_HEIGHT);
        shapes.setColor(0.85f, 0.15f, 0.35f, alpha);          // trenutni HP
        shapes.rect(x, y, hpWidth, BAR_HEIGHT);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.85f, 0.5f, 1f, alpha);              // vijoličen rob kot boss na minimapi
        shapes.rect(x, y, BAR_WIDTH, BAR_HEIGHT);
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        // ime na levi, HP na desni (rahla senca za berljivost na svetlem trailu)
        drawText(batch, name, x + TEXT_PADDING, y, false);
        String hpText = (int) Math.ceil(hp) + " / " + (int) Math.ceil(maxHp);
        drawText(batch, hpText, x + BAR_WIDTH - TEXT_PADDING, y, true);
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawText(SpriteBatch batch, String text, float anchorX, float barY, boolean rightAligned) {
        // barvo je treba nastaviti PRED setText, ker GlyphLayout vzame barvo takrat
        font.setColor(0f, 0f, 0f, alpha);
        layout.setText(font, text);
        float textX = rightAligned ? anchorX - layout.width : anchorX;
        float textY = barY + (BAR_HEIGHT + layout.height) / 2f;
        font.draw(batch, layout, textX + 1f, textY - 1f); // senca

        font.setColor(1f, 1f, 1f, alpha);
        layout.setText(font, text);
        font.draw(batch, layout, textX, textY);
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}

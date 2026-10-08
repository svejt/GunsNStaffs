package io.github.roguelike.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.GdxGame;
import io.github.roguelike.entity.Player;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 480; // 4:3

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Player player = new Player(WORLD_W / 2f, WORLD_H / 2f);

    public GameScreen(GdxGame game) { this.game = game; }

    @Override
    public void render(float delta) {
        player.update(delta);
        player.clampToWorld(WORLD_W, WORLD_H);

        // celoten zaslon (tudi letterbox robovi) črn
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // pobarvaj samo območje igre
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.1f, 0.12f, 1f);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        player.render(batch);
        batch.end();
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        player.dispose();
    }
}

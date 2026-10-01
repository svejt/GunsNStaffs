package io.github.roguelike.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.entity.Player;

public class GameScreen<GunsNStaffs> extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 360;

    private final GunsNStaffs game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final Player player = new Player(WORLD_W / 2f, WORLD_H / 2f);

    public GameScreen(GunsNStaffs game) { this.game = game; }

    @Override
    public void render(float delta) {
        player.update(delta);
        player.clampToWorld(WORLD_W, WORLD_H);

        ScreenUtils.clear(0.1f, 0.1f, 0.12f, 1f);
        viewport.apply();
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
        player.dispose();
    }
}

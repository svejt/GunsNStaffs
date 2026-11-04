package io.github.roguelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.GdxGame;

public class StartMenuScreen extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 480;

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();

    // Button settings
    private final float buttonX = WORLD_W / 2f - 100f;
    private final float buttonY = WORLD_H / 2f - 30f;
    private final float buttonW = 200f;
    private final float buttonH = 60f;

    public StartMenuScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        handleInput();

        ScreenUtils.clear(0.1f, 0.1f, 0.12f, 1f);

        viewport.apply();

        // Draw title
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.getData().setScale(3f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Guns N Staffs", WORLD_W / 2f - 180f, WORLD_H / 2f + 100f);
        batch.end();

        // Draw start button
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.5f, 0.8f, 1f);
        shapes.rect(buttonX, buttonY, buttonW, buttonH);
        shapes.end();

        // Button border
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(buttonX, buttonY, buttonW, buttonH);
        shapes.end();

        // Button text
        batch.begin();
        font.getData().setScale(1.5f);
        font.draw(batch, "START", WORLD_W / 2f - 35f, WORLD_H / 2f + 5f);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.input.getY();

            // Convert to world coordinates
            mouseX = mouseX * WORLD_W / Gdx.graphics.getWidth();
            mouseY = WORLD_H - (mouseY * WORLD_H / Gdx.graphics.getHeight());

            // Check if click is on button
            if (mouseX >= buttonX && mouseX <= buttonX + buttonW &&
                mouseY >= buttonY && mouseY <= buttonY + buttonH) {
                game.setScreen(new GameScreen(game));
            }
        }

        // Also allow pressing SPACE or ENTER to start
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}

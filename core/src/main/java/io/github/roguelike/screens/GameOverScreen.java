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

public class GameOverScreen extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 480;

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();

    // Button settings
    private final float buttonX = WORLD_W / 2f - 100f;
    private final float buttonY = WORLD_H / 2f - 80f;
    private final float buttonW = 200f;
    private final float buttonH = 60f;

    public GameOverScreen(GdxGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        handleInput();

        // Dim the screen
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        viewport.apply();

        // Draw dim overlay
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.7f);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        // Draw Game Over title
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.getData().setScale(3f);
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", WORLD_W / 2f - 150f, WORLD_H / 2f + 50f);
        batch.end();

        // Draw menu button
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.8f, 0.5f, 0.3f, 1f);
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
        font.setColor(Color.WHITE);
        font.draw(batch, "MENU", WORLD_W / 2f - 35f, WORLD_H / 2f - 45f);
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
                game.setScreen(new StartMenuScreen(game));
            }
        }

        // Also allow pressing SPACE or ENTER to return to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new StartMenuScreen(game));
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

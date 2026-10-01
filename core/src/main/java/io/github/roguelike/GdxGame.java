package io.github.roguelike;

import com.badlogic.gdx.Game;
import io.github.roguelike.screens.GameScreen;

public class GdxGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        // sprosti trenutni screen, ko se igra zapre
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}

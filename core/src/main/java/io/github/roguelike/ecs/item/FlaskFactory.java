package io.github.roguelike.ecs.item;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.roguelike.ecs.component.FlaskComponent;
import io.github.roguelike.ecs.component.TransformComponent;

public class FlaskFactory implements Disposable {
    private static final int ITEM_SIZE = 32;

    private final Texture flaskTexture;
    private final TextureRegion flaskRegion;

    public FlaskFactory() {
        flaskTexture = new Texture(Gdx.files.internal("Items/flask.png"));
        flaskTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        flaskRegion = new TextureRegion(flaskTexture);
    }

    public Entity createFlask(Engine engine, float x, float y, float healAmount) {
        Entity flask = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y);
        transform.width = ITEM_SIZE;
        transform.height = ITEM_SIZE;

        FlaskComponent flaskComponent = new FlaskComponent();
        flaskComponent.region = flaskRegion;
        flaskComponent.healAmount = healAmount;

        flask.add(transform).add(flaskComponent);
        engine.addEntity(flask);
        return flask;
    }

    @Override
    public void dispose() {
        flaskTexture.dispose();
    }
}

package io.github.roguelike.ecs.item;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.roguelike.ecs.component.ItemComponent;
import io.github.roguelike.ecs.component.TransformComponent;

public class ItemFactory implements Disposable {
    private static final int ITEM_SIZE = 32;

    private final Texture boxTexture;
    private final TextureRegion boxRegion;

    public ItemFactory() {
        boxTexture = new Texture(Gdx.files.internal("Items/box.png"));
        boxTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        boxRegion = new TextureRegion(boxTexture);
    }

    public Entity createItem(Engine engine, float x, float y) {
        Entity item = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y);
        transform.width = ITEM_SIZE;
        transform.height = ITEM_SIZE;

        ItemComponent itemComponent = new ItemComponent();
        itemComponent.region = boxRegion;

        item.add(transform).add(itemComponent);
        engine.addEntity(item);
        return item;
    }

    @Override
    public void dispose() {
        boxTexture.dispose();
    }
}

package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.roguelike.ecs.component.ItemComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;

public class ItemRenderSystem extends IteratingSystem {
    private final SpriteBatch batch;

    public ItemRenderSystem(SpriteBatch batch) {
        super(Family.all(TransformComponent.class, ItemComponent.class).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity item, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(item);
        ItemComponent itemComp = Mappers.ITEM.get(item);

        batch.draw(itemComp.region, t.position.x, t.position.y, t.width, t.height);
    }
}

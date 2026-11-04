package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.roguelike.ecs.component.FloatingTextComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.ecs.component.VelocityComponent;

public class FloatingTextRenderSystem extends IteratingSystem {
    private final SpriteBatch batch;
    private final BitmapFont font = new BitmapFont();

    public FloatingTextRenderSystem(SpriteBatch batch) {
        super(Family.all(TransformComponent.class, FloatingTextComponent.class).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(entity);
        FloatingTextComponent ft = Mappers.FLOATING_TEXT.get(entity);

        // Update lifetime
        ft.lifetime -= deltaTime;

        // Remove if expired
        if (ft.lifetime <= 0f) {
            getEngine().removeEntity(entity);
            return;
        }

        // Move upward
        t.position.y += ft.velocityY * deltaTime;

        // Fade out based on lifetime
        float alpha = ft.lifetime / ft.maxLifetime;
        Color color = new Color(1f, 1f, 0f, alpha); // Yellow with fade

        batch.begin();
        font.setColor(color);
        font.getData().setScale(1.2f);
        font.draw(batch, ft.text, t.position.x, t.position.y);
        batch.end();
    }

    public void dispose() {
        font.dispose();
    }
}

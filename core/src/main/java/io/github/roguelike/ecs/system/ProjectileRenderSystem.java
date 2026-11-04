package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.ProjectileComponent;
import io.github.roguelike.ecs.component.TransformComponent;

/**
 * Riše projectile. Ni v rednem engine.update() ciklu (processing = false),
 * ampak ga GameScreen pokliče ročno med batch.begin() in batch.end().
 */
public class ProjectileRenderSystem extends IteratingSystem {
    private final SpriteBatch batch;

    public ProjectileRenderSystem(SpriteBatch batch) {
        super(Family.all(ProjectileComponent.class, TransformComponent.class).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity projectile, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(projectile);
        ProjectileComponent p = projectile.getComponent(ProjectileComponent.class);
        batch.draw(p.region, t.position.x, t.position.y, t.width, t.height);
    }
}

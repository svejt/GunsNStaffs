package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.ProjectileComponent;
import io.github.roguelike.ecs.component.TransformComponent;

/** Odstrani projectile, ki so povsem zapustili svet. Premikanje dela MovementSystem. */
public class ProjectileSystem extends IteratingSystem {
    private final float worldW, worldH;

    public ProjectileSystem(float worldW, float worldH) {
        super(Family.all(ProjectileComponent.class, TransformComponent.class).get(), 3);
        this.worldW = worldW;
        this.worldH = worldH;
    }

    @Override
    protected void processEntity(Entity projectile, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(projectile);
        boolean outside = t.position.x + t.width < 0 || t.position.x > worldW
            || t.position.y + t.height < 0 || t.position.y > worldH;

        // Ashley odstranitev med update-om počaka do konca iteracije, zato je to varno
        if (outside) getEngine().removeEntity(projectile);
    }
}

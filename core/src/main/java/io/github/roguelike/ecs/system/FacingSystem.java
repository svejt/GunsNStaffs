package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.component.FacingComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.VelocityComponent;
import io.github.roguelike.entity.Player.Direction;

/**
 * Smer gledanja ECS entitet = smer zadnjega premikanja (pri diagonali zmaga horizontala, kot pri Playerju).
 * Player ni ECS entiteta: njegov facing upravlja Player (hoja) in PlayerShootSystem (streljanje ima prednost).
 */
public class FacingSystem extends IteratingSystem {

    public FacingSystem() {
        super(Family.all(FacingComponent.class, VelocityComponent.class).get(), 2);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Vector2 v = Mappers.VELOCITY.get(entity).value;
        if (v.isZero()) return;

        Mappers.FACING.get(entity).value = Direction.fromVector(v.x, v.y);
    }
}

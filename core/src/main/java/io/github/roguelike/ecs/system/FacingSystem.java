package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.component.FacingComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.VelocityComponent;
import io.github.roguelike.entity.Player.Direction;

/** Smer gledanja = smer zadnjega premikanja (pri diagonali zmaga horizontala, kot pri Playerju). */
public class FacingSystem extends IteratingSystem {

    public FacingSystem() {
        super(Family.all(FacingComponent.class, VelocityComponent.class).get(), 2);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Vector2 v = Mappers.VELOCITY.get(entity).value;
        if (v.isZero()) return;

        FacingComponent facing = Mappers.FACING.get(entity);
        if (Math.abs(v.x) >= Math.abs(v.y)) {
            facing.value = v.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing.value = v.y > 0 ? Direction.UP : Direction.DOWN;
        }
    }
}

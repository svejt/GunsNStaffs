package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.ecs.component.VelocityComponent;

public class MovementSystem extends IteratingSystem {

    public MovementSystem() {
        super(Family.all(TransformComponent.class, VelocityComponent.class).get(), 1);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Mappers.TRANSFORM.get(entity).position.mulAdd(Mappers.VELOCITY.get(entity).value, deltaTime);
    }
}

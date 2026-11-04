package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.roguelike.ecs.component.FlaskComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;

public class FlaskRenderSystem extends IteratingSystem {
    private final SpriteBatch batch;

    public FlaskRenderSystem(SpriteBatch batch) {
        super(Family.all(TransformComponent.class, FlaskComponent.class).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity flask, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(flask);
        FlaskComponent flaskComp = Mappers.FLASK.get(flask);

        batch.draw(flaskComp.region, t.position.x, t.position.y, t.width, t.height);
    }
}

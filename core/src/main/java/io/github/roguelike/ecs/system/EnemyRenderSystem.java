package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.roguelike.ecs.component.*;

/**
 * Riše enemyje (samo telo; slash riše SlashRenderSystem). Ni v rednem engine.update() ciklu
 * (processing = false), ampak ga GameScreen pokliče ročno med batch.begin() in batch.end().
 */
public class EnemyRenderSystem extends IteratingSystem {
    private final SpriteBatch batch;

    public EnemyRenderSystem(SpriteBatch batch) {
        super(Family.all(
            TransformComponent.class,
            FacingComponent.class,
            AnimationComponent.class,
            EnemyStateComponent.class
        ).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        TransformComponent t = Mappers.TRANSFORM.get(enemy);
        EnemyStateComponent sc = Mappers.ENEMY_STATE.get(enemy);

        Animation<TextureRegion> anim = Mappers.ANIMATION.get(enemy)
            .animations.get(sc.fsm.getCurrentState())
            .get(Mappers.FACING.get(enemy).value);

        batch.draw(anim.getKeyFrame(sc.stateTime), t.position.x, t.position.y, t.width, t.height);
    }
}

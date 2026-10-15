package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.roguelike.ecs.component.*;
import io.github.roguelike.ecs.enemy.EnemyState;
import io.github.roguelike.entity.Player.Direction;

/**
 * Riše slash efekte enemyjev. Je ločen od EnemyRenderSystem, da ga GameScreen lahko
 * pokliče PO risanju playerja in je efekt vedno v ospredju.
 * Ni v rednem engine.update() ciklu (processing = false).
 */
public class SlashRenderSystem extends IteratingSystem {
    // koliko px od središča enemyja se slash začne (v smeri gledanja)
    private static final float SLASH_OFFSET = 8f;

    private final SpriteBatch batch;

    public SlashRenderSystem(SpriteBatch batch) {
        super(Family.all(
            TransformComponent.class,
            FacingComponent.class,
            AttackComponent.class,
            EnemyStateComponent.class
        ).get());
        this.batch = batch;
        setProcessing(false);
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        EnemyStateComponent sc = Mappers.ENEMY_STATE.get(enemy);
        if (sc.fsm.getCurrentState() != EnemyState.ATTACK) return;

        TransformComponent t = Mappers.TRANSFORM.get(enemy);
        Direction facing = Mappers.FACING.get(enemy).value;

        TextureRegion frame = Mappers.ATTACK.get(enemy).slash.getKeyFrame(sc.stateTime);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

        float cx = t.position.x + t.width / 2f;
        float cy = t.position.y + t.height / 2f;

        // sličica je narisana v desno; ostale smeri dobimo z zrcaljenjem/rotacijo okoli središča enemyja
        float scaleX = 1f;
        float rotation = 0f;
        switch (facing) {
            case RIGHT: break;
            case LEFT:  scaleX = -1f; break;
            case UP:    rotation = 90f; break;
            case DOWN:  rotation = -90f; break;
        }

        // origin (rotacijska točka) = središče enemyja, relativno na levi rob sličice
        batch.draw(frame,
            cx + SLASH_OFFSET, cy - h / 2f,
            -SLASH_OFFSET, h / 2f,
            w, h,
            scaleX, 1f,
            rotation);
    }
}

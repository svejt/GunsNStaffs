package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import io.github.roguelike.ecs.component.EnemyComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.StatsComponent;
import io.github.roguelike.ecs.component.StatusEffectsComponent;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.screens.GameScreen;

/**
 * Posodablja statusne učinke enemyjev: izteka slow/stun, povzroča burn damage in izvaja knockback.
 * Slow in stun upošteva EnemyStateSystem (ta nastavi velocity), knockback premika position neposredno.
 */
public class StatusEffectSystem extends IteratingSystem {
    // koliko hitrosti odrivanja se izgubi na sekundo (6 -> odriv traja ~0.17 s)
    private static final float KNOCKBACK_DRAG = 6f;

    private final GameScreen gameScreen;
    private final float worldW, worldH, margin;

    public StatusEffectSystem(GameScreen gameScreen, float worldW, float worldH, float margin) {
        super(Family.all(
            EnemyComponent.class,
            StatusEffectsComponent.class,
            StatsComponent.class,
            TransformComponent.class
        ).get(), 1);
        this.gameScreen = gameScreen;
        this.worldW = worldW;
        this.worldH = worldH;
        this.margin = margin;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        StatusEffectsComponent status = Mappers.STATUS.get(enemy);
        StatsComponent stats = Mappers.STATS.get(enemy);
        TransformComponent t = Mappers.TRANSFORM.get(enemy);

        // enemy, ki je že padel (npr. zadet v tem frameu), se ne obdeluje več
        if (stats.currentHp <= 0f) return;

        if (status.slowTimer > 0f) {
            status.slowTimer -= deltaTime;
            if (status.slowTimer <= 0f) {
                status.slowTimer = 0f;
                status.slowMultiplier = 1f;
            }
        }

        if (status.stunTimer > 0f) {
            status.stunTimer = Math.max(0f, status.stunTimer - deltaTime);
        }

        if (status.burnTimer > 0f) {
            float tick = Math.min(deltaTime, status.burnTimer); // zadnji tick ne preseže trajanja
            stats.currentHp -= status.burnDps * tick;
            status.burnTimer -= deltaTime;
            if (status.burnTimer <= 0f) {
                status.burnTimer = 0f;
                status.burnDps = 0f;
            }
            if (stats.currentHp <= 0f) {
                if (stats.isBoss) gameScreen.onBossDeath();
                getEngine().removeEntity(enemy);
                return;
            }
        }

        if (status.knockback.len2() < 1f) {
            status.knockback.setZero();
        } else {
            t.position.mulAdd(status.knockback, deltaTime);
            // ne pusti enemyja skozi steno
            t.position.x = MathUtils.clamp(t.position.x, margin, worldW - margin - t.width);
            t.position.y = MathUtils.clamp(t.position.y, margin, worldH - margin - t.height);
            status.knockback.scl(Math.max(0f, 1f - KNOCKBACK_DRAG * deltaTime));
        }
    }
}

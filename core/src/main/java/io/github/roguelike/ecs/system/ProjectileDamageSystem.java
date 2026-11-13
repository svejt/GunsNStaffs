package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import io.github.roguelike.ecs.component.*;
import io.github.roguelike.screens.GameScreen;

/**
 * System za detekcijo trka med projectile in enemyji.
 * Če se projectile prekriva z enemyjem, mu nanesemo škodo.
 * Če enemyjev HP pade na 0 ali manj, ga odstranimo.
 */
public class ProjectileDamageSystem extends IteratingSystem {
    private final GameScreen gameScreen;
    private final Rectangle projectileBounds = new Rectangle();
    private final Rectangle enemyBounds = new Rectangle();

    public ProjectileDamageSystem(GameScreen gameScreen) {
        super(Family.all(
            ProjectileComponent.class,
            TransformComponent.class
        ).get(), 2); // teče po MovementSystem
        this.gameScreen = gameScreen;
    }

    /** Prenese učinek staffa (slow/burn/stun/knockback) na zadetega enemyja. */
    private void applyEffect(Entity enemy, Entity projectile, ProjectileComponent projComp) {
        StatusEffectsComponent status = Mappers.STATUS.get(enemy);
        if (status == null) return;

        switch (projComp.effect) {
            case SLOW:
                status.slowTimer = projComp.effectDuration;
                status.slowMultiplier = projComp.effectPower;
                break;
            case BURN:
                status.burnTimer = projComp.effectDuration; // ponoven zadetek osveži trajanje
                status.burnDps = projComp.damage * projComp.effectPower;
                break;
            case STUN:
                status.stunTimer = Math.max(status.stunTimer, projComp.effectDuration);
                break;
            case KNOCKBACK:
                // smer odriva = smer leta projectila (stran od playerja / mesta izstrela)
                status.knockback.set(Mappers.VELOCITY.get(projectile).value).nor().scl(projComp.effectPower);
                break;
            default:
                break;
        }
    }

    @Override
    protected void processEntity(Entity projectile, float deltaTime) {
        TransformComponent projT = Mappers.TRANSFORM.get(projectile);
        ProjectileComponent projComp = Mappers.PROJECTILE.get(projectile);

        projectileBounds.set(projT.position.x, projT.position.y, projT.width, projT.height);

        // Preveri trk z vsemi enemyji
        for (Entity enemy : getEngine().getEntitiesFor(Family.all(
            EnemyComponent.class,
            TransformComponent.class,
            StatsComponent.class
        ).get())) {
            TransformComponent enemyT = Mappers.TRANSFORM.get(enemy);
            StatsComponent enemyStats = Mappers.STATS.get(enemy);

            // že padel enemy (npr. zadet v istem frameu ali od burna) ne sprejema več zadetkov
            if (enemyStats.currentHp <= 0f) continue;

            enemyBounds.set(enemyT.position.x, enemyT.position.y, enemyT.width, enemyT.height);

            if (projectileBounds.overlaps(enemyBounds)) {
                // Nanesi škodo
                enemyStats.currentHp -= projComp.damage;
                applyEffect(enemy, projectile, projComp);

                // Odstrani projectile
                getEngine().removeEntity(projectile);

                // Če je enemy mrtev, ga odstrani
                if (enemyStats.currentHp <= 0f) {
                    if (enemyStats.isBoss) {
                        gameScreen.onBossDeath();
                    }
                    getEngine().removeEntity(enemy);
                }

                // Projectile je zadel samo enega enemyja, končaj
                return;
            }
        }
    }
}

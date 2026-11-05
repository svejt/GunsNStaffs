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

            enemyBounds.set(enemyT.position.x, enemyT.position.y, enemyT.width, enemyT.height);

            if (projectileBounds.overlaps(enemyBounds)) {
                // Nanesi škodo
                enemyStats.currentHp -= projComp.damage;

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

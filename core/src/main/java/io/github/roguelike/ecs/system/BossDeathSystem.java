package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.StatsComponent;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.screens.GameScreen;

public class BossDeathSystem extends IteratingSystem {
    private final GameScreen gameScreen;

    // Elite enemies have more than 100 HP (base 30 * 5 = 150 minimum)
    private static final float ELITE_HP_THRESHOLD = 100f;

    public BossDeathSystem(GameScreen gameScreen) {
        super(Family.all(TransformComponent.class, StatsComponent.class).get(), 0);
        this.gameScreen = gameScreen;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = Mappers.STATS.get(entity);

        // Check if this is an elite enemy (boss) that just died
        if (stats.maxHp > ELITE_HP_THRESHOLD && stats.currentHp <= 0f) {
            gameScreen.onBossDeath();
        }
    }
}

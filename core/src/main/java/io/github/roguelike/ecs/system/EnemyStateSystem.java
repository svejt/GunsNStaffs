package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.component.*;
import io.github.roguelike.ecs.enemy.EnemyState;
import io.github.roguelike.entity.Player;

/** Posodobi tarčo (player) in požene state machine vsakega enemyja. */
public class EnemyStateSystem extends IteratingSystem {
    private final Player player;
    private final Vector2 playerCenter = new Vector2();

    public EnemyStateSystem(Player player) {
        super(Family.all(
            EnemyComponent.class,
            EnemyStateComponent.class,
            AttackComponent.class,
            TargetComponent.class,
            TransformComponent.class,
            VelocityComponent.class
        ).get(), 0);
        this.player = player;
    }

    @Override
    public void update(float deltaTime) {
        // središče playerja izračunamo enkrat na frame, ne za vsakega enemyja posebej
        player.getBounds().getCenter(playerCenter);
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        Mappers.TARGET.get(enemy).position.set(playerCenter);

        AttackComponent attack = Mappers.ATTACK.get(enemy);
        attack.cooldownTimer = Math.max(0f, attack.cooldownTimer - deltaTime);

        EnemyStateComponent sc = Mappers.ENEMY_STATE.get(enemy);
        sc.stateTime += deltaTime;

        StatusEffectsComponent status = Mappers.STATUS.get(enemy);
        if (status != null && status.isStunned()) {
            // stun: prekine napad (slash in damage se ustavita), enemy stoji in ne premišljuje
            if (sc.fsm.getCurrentState() == EnemyState.ATTACK) sc.fsm.changeState(EnemyState.IDLE);
            Mappers.VELOCITY.get(enemy).value.setZero();
            return;
        }

        sc.fsm.update();

        // slow: velocity nastavi state machine vsak frame, zato množenje ni kumulativno
        if (status != null && status.isSlowed()) {
            Mappers.VELOCITY.get(enemy).value.scl(status.slowMultiplier);
        }
    }
}

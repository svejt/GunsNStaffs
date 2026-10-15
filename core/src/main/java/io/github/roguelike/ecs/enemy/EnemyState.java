package io.github.roguelike.ecs.enemy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.component.AttackComponent;
import io.github.roguelike.ecs.component.EnemyComponent;
import io.github.roguelike.ecs.component.EnemyStateComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;

/**
 * Stanja basic melee enemyja (gdx-ai state machine).
 * Owner stanja je Ashley Entity, podatke pa beremo/pišemo prek komponent.
 *
 * IDLE   -> player izven dosega: CHASE; v dosegu in cooldown mimo: ATTACK
 * CHASE  -> v dosegu: ATTACK (ali IDLE, če cooldown še teče)
 * ATTACK -> ko se slash animacija konča: IDLE
 */
public enum EnemyState implements State<Entity> {

    // stoji (čaka na cooldown ali na to, da se player premakne)
    IDLE {
        @Override
        public void enter(Entity enemy) {
            Mappers.VELOCITY.get(enemy).value.setZero();
            Mappers.ENEMY_STATE.get(enemy).stateTime = 0f;
        }

        @Override
        public void update(Entity enemy) {
            AttackComponent attack = Mappers.ATTACK.get(enemy);
            float dist = computeToTarget(enemy);

            if (dist > attack.range) {
                changeState(enemy, CHASE);
            } else if (attack.cooldownTimer <= 0f) {
                changeState(enemy, ATTACK);
            }
        }
    },

    // premika se proti playerju
    CHASE {
        @Override
        public void enter(Entity enemy) {
            Mappers.ENEMY_STATE.get(enemy).stateTime = 0f;
        }

        @Override
        public void update(Entity enemy) {
            EnemyComponent stats = Mappers.ENEMY.get(enemy);
            AttackComponent attack = Mappers.ATTACK.get(enemy);
            float dist = computeToTarget(enemy);

            if (dist <= attack.range) {
                changeState(enemy, attack.cooldownTimer <= 0f ? ATTACK : IDLE);
                return;
            }
            Mappers.VELOCITY.get(enemy).value.set(toTarget).nor().scl(stats.speed);
        }
    },

    // stoji in predvaja slash v smeri, kamor gleda
    ATTACK {
        @Override
        public void enter(Entity enemy) {
            Mappers.VELOCITY.get(enemy).value.setZero();
            Mappers.ENEMY_STATE.get(enemy).stateTime = 0f;
        }

        @Override
        public void update(Entity enemy) {
            AttackComponent attack = Mappers.ATTACK.get(enemy);
            if (Mappers.ENEMY_STATE.get(enemy).stateTime >= attack.slash.getAnimationDuration()) {
                changeState(enemy, IDLE);
            }
        }

        @Override
        public void exit(Entity enemy) {
            // cooldown se začne šele, ko je napad končan
            AttackComponent attack = Mappers.ATTACK.get(enemy);
            attack.cooldownTimer = attack.cooldown;
        }
    };
    // kasneje: HURT, DEAD ...

    // pomožni vektor, da ne alociramo vsak frame (igra je single-threaded)
    private static final Vector2 toTarget = new Vector2();

    /** Izračuna vektor od središča enemyja do tarče v toTarget in vrne njegovo dolžino. */
    private static float computeToTarget(Entity enemy) {
        TransformComponent t = Mappers.TRANSFORM.get(enemy);
        Vector2 target = Mappers.TARGET.get(enemy).position;
        toTarget.set(target).sub(t.position.x + t.width / 2f, t.position.y + t.height / 2f);
        return toTarget.len();
    }

    private static void changeState(Entity enemy, EnemyState next) {
        EnemyStateComponent sc = Mappers.ENEMY_STATE.get(enemy);
        sc.fsm.changeState(next);
    }

    @Override
    public void exit(Entity enemy) { }

    @Override
    public boolean onMessage(Entity enemy, Telegram telegram) {
        return false;
    }
}

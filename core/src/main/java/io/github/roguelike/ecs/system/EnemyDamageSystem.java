package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.component.*;
import io.github.roguelike.ecs.enemy.EnemyState;
import io.github.roguelike.entity.Player;
import io.github.roguelike.entity.Player.Direction;

/**
 * System za detekcijo trka med enemyjevim slash napadom in playerjem.
 * Če se slash prekriva s playerjem, mu nanesemo škodo.
 */
public class EnemyDamageSystem extends IteratingSystem {
    private static final float SLASH_OFFSET = 8f;

    private final Player player;
    private final Vector2 enemyCenter = new Vector2();
    private final Rectangle slashBounds = new Rectangle();
    private final Rectangle playerBounds = new Rectangle();

    public EnemyDamageSystem(Player player) {
        super(Family.all(
            TransformComponent.class,
            FacingComponent.class,
            AttackComponent.class,
            EnemyStateComponent.class,
            StatsComponent.class
        ).get(), 1); // teče po EnemyStateSystem
        this.player = player;
    }

    @Override
    protected void processEntity(Entity enemy, float deltaTime) {
        EnemyStateComponent sc = Mappers.ENEMY_STATE.get(enemy);
        if (sc.fsm.getCurrentState() != EnemyState.ATTACK) {
            // Reset damage flag when not attacking
            Mappers.ATTACK.get(enemy).hasDealtDamage = false;
            return;
        }

        AttackComponent attack = Mappers.ATTACK.get(enemy);
        if (attack.hasDealtDamage) return;

        TransformComponent t = Mappers.TRANSFORM.get(enemy);
        Direction facing = Mappers.FACING.get(enemy).value;

        // Calculate slash bounds (same logic as SlashRenderSystem)
        float cx = t.position.x + t.width / 2f;
        float cy = t.position.y + t.height / 2f;
        float slashW = attack.slash.getKeyFrame(0).getRegionWidth();
        float slashH = attack.slash.getKeyFrame(0).getRegionHeight();

        // Slash position based on facing direction
        float slashX, slashY;
        switch (facing) {
            case RIGHT:
                slashX = cx + SLASH_OFFSET;
                slashY = cy - slashH / 2f;
                break;
            case LEFT:
                slashX = cx - SLASH_OFFSET - slashW;
                slashY = cy - slashH / 2f;
                break;
            case UP:
                slashX = cx - slashH / 2f;
                slashY = cy + SLASH_OFFSET;
                // Swap width/height for vertical slash
                float temp = slashW;
                slashW = slashH;
                slashH = temp;
                break;
            case DOWN:
                slashX = cx - slashH / 2f;
                slashY = cy - SLASH_OFFSET - slashW;
                // Swap width/height for vertical slash
                float temp2 = slashW;
                slashW = slashH;
                slashH = temp2;
                break;
            default:
                return;
        }

        slashBounds.set(slashX, slashY, slashW, slashH);
        playerBounds.set(player.getBounds());

        // Check collision
        if (slashBounds.overlaps(playerBounds)) {
            StatsComponent stats = Mappers.STATS.get(enemy);
            float damage = stats.damage;
            player.setCurrentHp(player.getCurrentHp() - damage);
            attack.hasDealtDamage = true;
        }
    }
}

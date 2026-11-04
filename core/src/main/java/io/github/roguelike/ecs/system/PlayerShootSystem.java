package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import io.github.roguelike.ecs.projectile.ProjectileFactory;
import io.github.roguelike.entity.Player;

/**
 * Puščične tipke določajo smer streljanja (kombinacija = diagonala).
 * Dokler je tipka držana, player strelja vsakih FIRE_INTERVAL sekund.
 */
public class PlayerShootSystem extends EntitySystem {
    private static final float FIRE_INTERVAL = 0.25f;

    private final Player player;
    private final ProjectileFactory projectileFactory;

    private final Vector2 aim = new Vector2();
    private final Vector2 playerCenter = new Vector2();
    private float cooldown = 0f;

    public PlayerShootSystem(Player player, ProjectileFactory projectileFactory) {
        super(0);
        this.player = player;
        this.projectileFactory = projectileFactory;
    }

    @Override
    public void update(float deltaTime) {
        cooldown = Math.max(0f, cooldown - deltaTime);

        aim.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    aim.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  aim.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  aim.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) aim.x += 1;

        // nasprotni tipki se izničita (npr. levo+desno) -> ne strelja
        if (aim.isZero() || cooldown > 0f) return;

        player.getBounds().getCenter(playerCenter);
        projectileFactory.createProjectile(getEngine(), playerCenter.x, playerCenter.y, aim, player.getDamage());
        cooldown = FIRE_INTERVAL;
    }
}

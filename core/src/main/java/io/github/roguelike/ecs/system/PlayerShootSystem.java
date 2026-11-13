package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.roguelike.ecs.projectile.ProjectileFactory;
import io.github.roguelike.entity.Player;
import io.github.roguelike.weapon.WeaponType;

/**
 * Smer streljanja: puščične tipke (kombinacija = diagonala) ali, če ni nobena pritisnjena,
 * levi miškin gumb (smer proti kazalcu). Dokler player strelja, gleda v smer streljanja
 * (Player.aimAt); ko preneha, se facing spet ravna po hoji.
 * Hitrost, damage, število izstrelkov in učinek (staffi) določa aktivno orožje (Player.getActiveWeapon()).
 */
public class PlayerShootSystem extends EntitySystem {
    private final Player player;
    private final ProjectileFactory projectileFactory;
    private final Viewport viewport;

    private final Vector2 aim = new Vector2();
    private final Vector2 shotDirection = new Vector2();
    private final Vector2 mouse = new Vector2();
    private final Vector2 playerCenter = new Vector2();
    private final Vector2 muzzle = new Vector2();
    private float cooldown = 0f;
    private WeaponType lastWeapon;

    public PlayerShootSystem(Player player, ProjectileFactory projectileFactory, Viewport viewport) {
        super(0);
        this.player = player;
        this.projectileFactory = projectileFactory;
        this.viewport = viewport;
        this.lastWeapon = player.getActiveWeapon();
    }

    @Override
    public void update(float deltaTime) {
        cooldown = Math.max(0f, cooldown - deltaTime);

        // ob zamenjavi orožja cooldown ne sme biti daljši od intervala novega orožja
        WeaponType weapon = player.getActiveWeapon();
        if (weapon != lastWeapon) {
            cooldown = Math.min(cooldown, weapon.fireInterval);
            lastWeapon = weapon;
        }

        readAim();

        // nasprotni tipki se izničita (npr. levo+desno) -> ne strelja
        if (aim.isZero()) {
            player.stopAiming();
            return;
        }
        player.aimAt(aim); // gleda v smer streljanja tudi med cooldownom

        if (cooldown > 0f) return;

        // izstrelki zapustijo konico cevi, smer pa je smer ciljanja
        player.getMuzzlePosition(muzzle);
        for (int i = 0; i < weapon.pellets; i++) {
            float offset = weapon.pellets == 1
                ? 0f
                : -weapon.spreadDeg / 2f + weapon.spreadDeg * i / (weapon.pellets - 1);
            offset += MathUtils.random(-weapon.jitterDeg, weapon.jitterDeg);

            shotDirection.set(aim).nor().rotateDeg(offset);
            projectileFactory.createProjectile(getEngine(), muzzle.x, muzzle.y, shotDirection,
                player.getDamage() * weapon.damageMultiplier, weapon);
        }
        cooldown = weapon.fireInterval;
    }

    private void readAim() {
        aim.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    aim.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  aim.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  aim.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) aim.x += 1;

        // miška: samo če ni pritisnjena nobena puščica (tudi nasprotni par, da ne "uide" v miško)
        boolean anyArrow = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (!anyArrow && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(mouse); // zaslon -> svetovne koordinate (upošteva letterbox)
            player.getBounds().getCenter(playerCenter);
            aim.set(mouse).sub(playerCenter);
        }
    }
}

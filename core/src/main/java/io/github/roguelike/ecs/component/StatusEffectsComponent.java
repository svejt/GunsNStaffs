package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/** Aktivni statusni učinki na enemyju; timerje zmanjšuje StatusEffectSystem. */
public class StatusEffectsComponent implements Component {
    // slow
    public float slowTimer = 0f;
    public float slowMultiplier = 1f;
    // burn
    public float burnTimer = 0f;
    public float burnDps = 0f;
    // stun
    public float stunTimer = 0f;
    // knockback: trenutna hitrost odrivanja (px/s), ki se sama pojema
    public final Vector2 knockback = new Vector2();

    public boolean isSlowed()  { return slowTimer > 0f; }
    public boolean isBurning() { return burnTimer > 0f; }
    public boolean isStunned() { return stunTimer > 0f; }
}

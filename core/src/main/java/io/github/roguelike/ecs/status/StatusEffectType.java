package io.github.roguelike.ecs.status;

/** Učinek, ki ga projectile ob zadetku prenese na enemyja. */
public enum StatusEffectType {
    NONE,
    /** Zmanjša hitrost gibanja (effectPower = množitelj hitrosti, npr. 0.5 = polovična). */
    SLOW,
    /** Damage skozi čas (damage na sekundo = projectile damage * effectPower). */
    BURN,
    /** Enemy se ne more premikati ali napadati. */
    STUN,
    /** Odrine enemyja stran od projectila (effectPower = začetna hitrost odrivanja v px/s). */
    KNOCKBACK
}

package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.roguelike.ecs.status.StatusEffectType;

public class ProjectileComponent implements Component {
    // kar se nariše; za zdaj oranžen krog, kasneje lahko prava tekstura (nastavi se v ProjectileFactory)
    public TextureRegion region;
    // damage, ki ga projectile nanesi enemyju
    public float damage;

    // učinek, ki se ob zadetku prenese na enemyja (staffi); glej StatusEffectType za pomen effectPower
    public StatusEffectType effect = StatusEffectType.NONE;
    public float effectDuration = 0f;
    public float effectPower = 0f;
}

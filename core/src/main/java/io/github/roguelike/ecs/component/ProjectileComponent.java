package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ProjectileComponent implements Component {
    // kar se nariše; za zdaj oranžen krog, kasneje lahko prava tekstura (nastavi se v ProjectileFactory)
    public TextureRegion region;
    // damage, ki ga projectile nanesi enemyju
    public float damage;
}

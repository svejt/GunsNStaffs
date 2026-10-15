package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AttackComponent implements Component {
    // razdalja (središče-središče), pri kateri enemy začne napad
    public float range = 30f;
    // premor po koncu napada (sekunde)
    public float cooldown = 0.6f;
    // preostali cooldown; zmanjšuje ga EnemyStateSystem
    public float cooldownTimer = 0f;

    // slash efekt, narisan v desno; za ostale smeri se zavrti/zrcali pri risanju
    public Animation<TextureRegion> slash;
}

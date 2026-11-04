package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FlaskComponent implements Component {
    public TextureRegion region;
    public float healAmount; // Percentage of max HP to heal (0.1 = 10%)
}

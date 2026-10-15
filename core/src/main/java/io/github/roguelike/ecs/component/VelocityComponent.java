package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class VelocityComponent implements Component {
    // piksli na sekundo
    public final Vector2 value = new Vector2();
}

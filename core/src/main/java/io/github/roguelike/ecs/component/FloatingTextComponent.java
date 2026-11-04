package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;

public class FloatingTextComponent implements Component {
    public String text;
    public float lifetime;
    public float maxLifetime;
    public float velocityY = 30f; // Float upward
}

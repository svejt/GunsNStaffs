package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;

/** Oznaka "to je enemy" + njegovi osnovni parametri. */
public class EnemyComponent implements Component {
    public float speed = 100f;
}

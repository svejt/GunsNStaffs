package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TargetComponent implements Component {
    // središče tarče (playerja); osvežuje ga EnemyStateSystem vsak frame
    public final Vector2 position = new Vector2();
}

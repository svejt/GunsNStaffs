package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import io.github.roguelike.entity.Player.Direction;

public class FacingComponent implements Component {
    public Direction value = Direction.DOWN;
}

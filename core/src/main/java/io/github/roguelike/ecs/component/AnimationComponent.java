package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.roguelike.ecs.enemy.EnemyState;
import io.github.roguelike.entity.Player.Direction;

import java.util.EnumMap;
import java.util.Map;

public class AnimationComponent implements Component {
    // stanje -> smer -> animacija
    public final Map<EnemyState, Map<Direction, Animation<TextureRegion>>> animations =
        new EnumMap<>(EnemyState.class);
}

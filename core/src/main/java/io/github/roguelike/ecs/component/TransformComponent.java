package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component {
    // spodnji levi kot (enako kot v entity.Entity)
    public final Vector2 position = new Vector2();
    public float width, height;

    public Vector2 getCenter(Vector2 out) {
        return out.set(position.x + width / 2f, position.y + height / 2f);
    }
}

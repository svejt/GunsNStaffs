package io.github.roguelike.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected final Vector2 position = new Vector2();
    protected final Vector2 velocity = new Vector2();
    protected float width, height, speed;
    protected int hp, maxHp;

    public Entity(float x, float y, float width, float height) {
        position.set(x, y);
        this.width = width;
        this.height = height;
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }
}

package io.github.roguelike.ecs.projectile;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import io.github.roguelike.ecs.component.ProjectileComponent;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.ecs.component.VelocityComponent;

/** Ustvarja projectile. Za zdaj je projectile oranžen krog, narisan v kodi (brez asset datoteke). */
public class ProjectileFactory implements Disposable {
    private static final int DIAMETER = 12;
    private static final float SPEED = 400f;

    private final Texture texture;
    private final TextureRegion region;

    public ProjectileFactory() {
        Pixmap pixmap = new Pixmap(DIAMETER, DIAMETER, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 0.55f, 0f, 1f); // oranžna
        pixmap.fillCircle(DIAMETER / 2, DIAMETER / 2, DIAMETER / 2 - 1);
        texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // ko boš imel pravo teksturo, zamenjaj zgornje z: new Texture(Gdx.files.internal("..."))
        region = new TextureRegion(texture);
    }

    /** Ustvari projectile s središčem v (centerX, centerY), ki leti v smeri direction (ni nujno normalizirana). */
    public Entity createProjectile(Engine engine, float centerX, float centerY, Vector2 direction, float damage) {
        Entity projectile = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.width = DIAMETER;
        transform.height = DIAMETER;
        transform.position.set(centerX - DIAMETER / 2f, centerY - DIAMETER / 2f);

        VelocityComponent velocity = new VelocityComponent();
        velocity.value.set(direction).nor().scl(SPEED);

        ProjectileComponent projectileComponent = new ProjectileComponent();
        projectileComponent.region = region;
        projectileComponent.damage = damage;

        projectile.add(transform).add(velocity).add(projectileComponent);
        engine.addEntity(projectile);
        return projectile;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}

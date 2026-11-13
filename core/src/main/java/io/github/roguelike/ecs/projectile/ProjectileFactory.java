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
import io.github.roguelike.ecs.status.StatusEffectType;
import io.github.roguelike.weapon.WeaponType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Ustvarja projectile. Vsako orožje ima svoj projectile (barva/velikost), narisan v kodi
 * (brez asset datoteke); staffi mu dodajo še učinek (slow/burn/stun/knockback).
 */
public class ProjectileFactory implements Disposable {
    private static final float SPEED = 400f;

    private final Map<WeaponType, Texture> textures = new EnumMap<>(WeaponType.class);
    private final Map<WeaponType, TextureRegion> regions = new EnumMap<>(WeaponType.class);

    public ProjectileFactory() {
        for (WeaponType type : WeaponType.values()) {
            int d = type.projectileSize;
            Pixmap pixmap = new Pixmap(d, d, Pixmap.Format.RGBA8888);
            pixmap.setColor(type.projectileColor);
            pixmap.fillCircle(d / 2, d / 2, d / 2 - 1);
            // svetlejše jedro, da se elementi ločijo tudi po obliki
            if (type.effect != StatusEffectType.NONE) {
                pixmap.setColor(1f, 1f, 1f, 0.6f);
                pixmap.fillCircle(d / 2, d / 2, Math.max(1, d / 5));
            }
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            textures.put(type, texture);
            regions.put(type, new TextureRegion(texture));
        }
    }

    /**
     * Ustvari projectile orožja weapon s središčem v (centerX, centerY), ki leti v smeri direction
     * (ni nujno normalizirana). Učinek staffa se prenese v ProjectileComponent.
     */
    public Entity createProjectile(Engine engine, float centerX, float centerY, Vector2 direction,
                                   float damage, WeaponType weapon) {
        Entity projectile = new Entity();
        float size = weapon.projectileSize;

        TransformComponent transform = new TransformComponent();
        transform.width = size;
        transform.height = size;
        transform.position.set(centerX - size / 2f, centerY - size / 2f);

        VelocityComponent velocity = new VelocityComponent();
        velocity.value.set(direction).nor().scl(SPEED);

        ProjectileComponent projectileComponent = new ProjectileComponent();
        projectileComponent.region = regions.get(weapon);
        projectileComponent.damage = damage;
        projectileComponent.effect = weapon.effect;
        projectileComponent.effectDuration = weapon.effectDuration;
        projectileComponent.effectPower = weapon.effectPower;

        projectile.add(transform).add(velocity).add(projectileComponent);
        engine.addEntity(projectile);
        return projectile;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures.values()) texture.dispose();
    }
}

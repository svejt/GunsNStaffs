package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import io.github.roguelike.ecs.component.FloatingTextComponent;
import io.github.roguelike.ecs.component.FlaskComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.entity.Player;

public class FlaskCollectionSystem extends IteratingSystem {
    private final Player player;
    private final Rectangle flaskBounds = new Rectangle();
    private final Rectangle playerBounds = new Rectangle();

    public FlaskCollectionSystem(Player player) {
        super(Family.all(TransformComponent.class, FlaskComponent.class).get(), 1);
        this.player = player;
    }

    @Override
    protected void processEntity(Entity flask, float deltaTime) {
        TransformComponent flaskT = Mappers.TRANSFORM.get(flask);
        FlaskComponent flaskComp = Mappers.FLASK.get(flask);

        flaskBounds.set(flaskT.position.x, flaskT.position.y, flaskT.width, flaskT.height);
        playerBounds.set(player.getBounds());

        if (flaskBounds.overlaps(playerBounds)) {
            // Heal player for 10% of max HP
            float healAmount = player.getMaxHp() * flaskComp.healAmount;
            player.setCurrentHp(Math.min(player.getCurrentHp() + healAmount, player.getMaxHp()));

            // Spawn floating text
            spawnFloatingText(flaskT.position.x, flaskT.position.y, "+" + (int)healAmount + " HP");

            // Remove flask
            getEngine().removeEntity(flask);
        }
    }

    private void spawnFloatingText(float x, float y, String text) {
        Entity floatingText = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y + 20f);

        FloatingTextComponent ft = new FloatingTextComponent();
        ft.text = text;
        ft.lifetime = 2f;
        ft.maxLifetime = 2f;

        floatingText.add(transform).add(ft);
        getEngine().addEntity(floatingText);
    }
}

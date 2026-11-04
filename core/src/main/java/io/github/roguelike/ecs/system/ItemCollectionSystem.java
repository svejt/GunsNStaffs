package io.github.roguelike.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import io.github.roguelike.ecs.component.FloatingTextComponent;
import io.github.roguelike.ecs.component.ItemComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.TransformComponent;
import io.github.roguelike.entity.Player;
import io.github.roguelike.screens.GameScreen;

public class ItemCollectionSystem extends IteratingSystem {
    private final Player player;
    private final GameScreen gameScreen;
    private final Rectangle itemBounds = new Rectangle();
    private final Rectangle playerBounds = new Rectangle();

    // Default stats for 10% calculation
    private static final float DEFAULT_MAX_HP = 50f;
    private static final float DEFAULT_DAMAGE = 10f;
    private static final float DEFAULT_ATTACK_RATE = 1f;
    private static final float DEFAULT_MOVEMENT_SPEED = 250f;

    // Elite enemy spawn settings
    private static final int ELITE_SPAWN_THRESHOLD = 20;

    public ItemCollectionSystem(Player player, GameScreen gameScreen) {
        super(Family.all(TransformComponent.class, ItemComponent.class).get(), 1);
        this.player = player;
        this.gameScreen = gameScreen;
    }

    @Override
    protected void processEntity(Entity item, float deltaTime) {
        TransformComponent itemT = Mappers.TRANSFORM.get(item);
        ItemComponent itemComp = Mappers.ITEM.get(item);

        itemBounds.set(itemT.position.x, itemT.position.y, itemT.width, itemT.height);
        playerBounds.set(player.getBounds());

        if (itemBounds.overlaps(playerBounds)) {
            // Apply random stat boost (except current HP) - 10% of default
            String statName = applyRandomStatBoost();

            // Spawn floating text
            spawnFloatingText(itemT.position.x, itemT.position.y, statName);

            // Increment box counter and check for elite spawn
            gameScreen.incrementBoxCounter();

            // Remove item
            getEngine().removeEntity(item);
        }
    }

    private String applyRandomStatBoost() {
        int statChoice = MathUtils.random(0, 3); // 0: maxHp, 1: damage, 2: attackRate, 3: movementSpeed
        String statName = "";

        switch (statChoice) {
            case 0: // Max HP
                float hpBoost = DEFAULT_MAX_HP * 0.1f;
                player.setMaxHp(player.getMaxHp() + hpBoost);
                statName = "+" + (int)hpBoost + " Max HP";
                break;
            case 1: // Damage
                float damageBoost = DEFAULT_DAMAGE * 0.1f;
                player.setDamage(player.getDamage() + damageBoost);
                statName = "+" + String.format("%.1f", damageBoost) + " Damage";
                break;
            case 2: // Attack Rate
                float attackRateBoost = DEFAULT_ATTACK_RATE * 0.1f;
                player.setAttackRate(player.getAttackRate() + attackRateBoost);
                statName = "+" + String.format("%.2f", attackRateBoost) + " Attack Rate";
                break;
            case 3: // Movement Speed
                float speedBoost = DEFAULT_MOVEMENT_SPEED * 0.1f;
                player.setMovementSpeed(player.getMovementSpeed() + speedBoost);
                statName = "+" + (int)speedBoost + " Speed";
                break;
        }

        return statName;
    }

    private void spawnFloatingText(float x, float y, String text) {
        Entity floatingText = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y + 20f); // Start slightly above the item

        FloatingTextComponent ft = new FloatingTextComponent();
        ft.text = text;
        ft.lifetime = 2f;
        ft.maxLifetime = 2f;

        floatingText.add(transform).add(ft);
        getEngine().addEntity(floatingText);
    }
}

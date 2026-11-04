package io.github.roguelike.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.GdxGame;
import io.github.roguelike.ecs.enemy.EnemyFactory;
import io.github.roguelike.ecs.item.FlaskFactory;
import io.github.roguelike.ecs.item.ItemFactory;
import io.github.roguelike.ecs.projectile.ProjectileFactory;
import io.github.roguelike.ecs.system.BossDeathSystem;
import io.github.roguelike.ecs.system.EnemyDamageSystem;
import io.github.roguelike.ecs.system.EnemyRenderSystem;
import io.github.roguelike.ecs.system.EnemyStateSystem;
import io.github.roguelike.ecs.system.FacingSystem;
import io.github.roguelike.ecs.system.FlaskCollectionSystem;
import io.github.roguelike.ecs.system.FlaskRenderSystem;
import io.github.roguelike.ecs.system.FloatingTextRenderSystem;
import io.github.roguelike.ecs.system.ItemCollectionSystem;
import io.github.roguelike.ecs.system.ItemRenderSystem;
import io.github.roguelike.ecs.system.MovementSystem;
import io.github.roguelike.ecs.system.PlayerShootSystem;
import io.github.roguelike.ecs.system.ProjectileDamageSystem;
import io.github.roguelike.ecs.system.ProjectileRenderSystem;
import io.github.roguelike.ecs.system.ProjectileSystem;
import io.github.roguelike.ecs.system.SlashRenderSystem;
import io.github.roguelike.entity.Player;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 480; // 4:3

    // Health bar settings
    private static final float HP_BAR_WIDTH = 150f;
    private static final float HP_BAR_HEIGHT = 15f;
    private static final float HP_BAR_PADDING = 10f;

    // Progress bar settings
    private static final float PROGRESS_BAR_WIDTH = 300f;
    private static final float PROGRESS_BAR_HEIGHT = 20f;
    private static final float PROGRESS_BAR_PADDING = 10f;
    private static final int BOXES_FOR_BOSS = 20;

    // Enemy spawn settings
    private static final float SPAWN_INTERVAL = 1f;
    private float spawnTimer = 0f;

    // Item spawn settings
    private static final float ITEM_SPAWN_INTERVAL = 5f;
    private float itemSpawnTimer = 0f;

    // Flask spawn settings
    private static final float FLASK_SPAWN_INTERVAL = 10f;
    private float flaskSpawnTimer = 0f;

    // Box pickup counter for elite enemy spawn
    private int boxesCollected = 0;

    // Difficulty multiplier (increases by 3x each boss kill)
    private float difficultyMultiplier = 1f;

    // Stage counter (increases each boss kill)
    private int stage = 1;

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Player player = new Player(WORLD_W / 2f, WORLD_H / 2f);

    // Ashley
    private final Engine engine = new Engine();
    private final EnemyFactory enemyFactory = new EnemyFactory();
    private final EnemyRenderSystem enemyRenderSystem = new EnemyRenderSystem(batch);
    private final SlashRenderSystem slashRenderSystem = new SlashRenderSystem(batch);
    private final ProjectileFactory projectileFactory = new ProjectileFactory();
    private final ProjectileRenderSystem projectileRenderSystem = new ProjectileRenderSystem(batch);
    private final ItemFactory itemFactory = new ItemFactory();
    private final ItemRenderSystem itemRenderSystem = new ItemRenderSystem(batch);
    private final FloatingTextRenderSystem floatingTextRenderSystem = new FloatingTextRenderSystem(batch);
    private final FlaskFactory flaskFactory = new FlaskFactory();
    private final FlaskRenderSystem flaskRenderSystem = new FlaskRenderSystem(batch);

    public GameScreen(GdxGame game) {
        this.game = game;

        engine.addSystem(new PlayerShootSystem(player, projectileFactory)); // puščice -> nov projectile
        engine.addSystem(new EnemyStateSystem(player)); // state machine -> nastavi velocity
        engine.addSystem(new MovementSystem());         // velocity -> position
        engine.addSystem(new FacingSystem());           // velocity -> smer gledanja
        engine.addSystem(new EnemyDamageSystem(player)); // slash collision -> player damage
        engine.addSystem(new ProjectileDamageSystem()); // projectile collision -> enemy damage
        engine.addSystem(new ItemCollectionSystem(player, this)); // item collision -> stat boost
        engine.addSystem(new FlaskCollectionSystem(player)); // flask collision -> heal
        engine.addSystem(new BossDeathSystem(this));    // boss death -> difficulty increase
        engine.addSystem(new ProjectileSystem(WORLD_W, WORLD_H)); // odstrani projectile izven sveta
        engine.addSystem(enemyRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(slashRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(projectileRenderSystem);       // processing = false, riše se ročno v render()
        engine.addSystem(itemRenderSystem);             // processing = false, riše se ročno v render()
        engine.addSystem(flaskRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(floatingTextRenderSystem);     // processing = false, riše se ročno v render()

        enemyFactory.createMeleeEnemy(engine, 40, 40, difficultyMultiplier);
        enemyFactory.createMeleeEnemy(engine, 90, 40, difficultyMultiplier);
        enemyFactory.createMeleeEnemy(engine, 140, 40, difficultyMultiplier);
    }

    private void spawnEnemyAtBorder() {
        float x, y;
        int side = MathUtils.random(0, 3); // 0: top, 1: right, 2: bottom, 3: left

        switch (side) {
            case 0: // top
                x = MathUtils.random(0f, WORLD_W);
                y = WORLD_H - 48f; // enemy height
                break;
            case 1: // right
                x = WORLD_W - 48f; // enemy width
                y = MathUtils.random(0f, WORLD_H);
                break;
            case 2: // bottom
                x = MathUtils.random(0f, WORLD_W);
                y = 0f;
                break;
            case 3: // left
                x = 0f;
                y = MathUtils.random(0f, WORLD_H);
                break;
            default:
                x = WORLD_W / 2f;
                y = WORLD_H / 2f;
        }

        enemyFactory.createMeleeEnemy(engine, x, y, difficultyMultiplier);
    }

    private void spawnItemAtRandom() {
        float x = MathUtils.random(32f, WORLD_W - 32f);
        float y = MathUtils.random(32f, WORLD_H - 32f);
        itemFactory.createItem(engine, x, y);
    }

    private void spawnFlaskAtRandom() {
        float x = MathUtils.random(32f, WORLD_W - 32f);
        float y = MathUtils.random(32f, WORLD_H - 32f);
        flaskFactory.createFlask(engine, x, y, 0.1f); // 10% heal
    }

    public void incrementBoxCounter() {
        boxesCollected++;
        if (boxesCollected % 20 == 0) {
            spawnEliteEnemy();
        }
    }

    private void spawnEliteEnemy() {
        float x = MathUtils.random(96f, WORLD_W - 96f);
        float y = MathUtils.random(96f, WORLD_H - 96f);
        // 96x96 size (2x multiplier), 5x stats * difficulty multiplier
        enemyFactory.createEliteEnemy(engine, x, y, 2f, 5f * difficultyMultiplier);
    }

    public void onBossDeath() {
        difficultyMultiplier *= 3f;
        stage++;
        boxesCollected = 0; // Reset box counter for next stage
    }

    public float getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    @Override
    public void render(float delta) {
        player.update(delta);
        player.clampToWorld(WORLD_W, WORLD_H);

        // Check for game over
        if (player.getCurrentHp() <= 0f) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        // Spawn enemies periodically
        spawnTimer += delta;
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnEnemyAtBorder();
            spawnTimer = 0f;
        }

        // Spawn items periodically
        itemSpawnTimer += delta;
        if (itemSpawnTimer >= ITEM_SPAWN_INTERVAL) {
            spawnItemAtRandom();
            itemSpawnTimer = 0f;
        }

        engine.update(delta);

        // celoten zaslon (tudi letterbox robovi) črn
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // pobarvaj samo območje igre
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.1f, 0.12f, 1f);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        enemyRenderSystem.update(delta);
        player.render(batch);
        projectileRenderSystem.update(delta);
        itemRenderSystem.update(delta);
        slashRenderSystem.update(delta); // slash čisto na koncu, da je pred playerjem
        batch.end();

        floatingTextRenderSystem.update(delta); // Render floating text after batch end

        // Draw health bar
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float hpBarX = WORLD_W - HP_BAR_WIDTH - HP_BAR_PADDING;
        float hpBarY = WORLD_H - HP_BAR_HEIGHT - HP_BAR_PADDING;
        float hpPercent = player.getCurrentHp() / player.getMaxHp();
        float currentHpWidth = HP_BAR_WIDTH * hpPercent;

        // Background (red)
        shapes.setColor(0.8f, 0.2f, 0.2f, 1f);
        shapes.rect(hpBarX, hpBarY, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        // Foreground (green - current health)
        shapes.setColor(0.2f, 0.8f, 0.2f, 1f);
        shapes.rect(hpBarX, hpBarY, currentHpWidth, HP_BAR_HEIGHT);

        // Border (white)
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(hpBarX, hpBarY, HP_BAR_WIDTH, HP_BAR_HEIGHT);
        shapes.end();

        // Draw stage text and progress bar at bottom
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        String stageText = "Stage " + stage;
        font.draw(batch, stageText, WORLD_W / 2f - 30f, PROGRESS_BAR_PADDING + PROGRESS_BAR_HEIGHT + 40f);
        batch.end();

        // Progress bar background
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float progressX = WORLD_W / 2f - PROGRESS_BAR_WIDTH / 2f;
        float progressY = PROGRESS_BAR_PADDING;
        shapes.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapes.rect(progressX, progressY, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        // Progress bar fill
        float progress = (float) boxesCollected / BOXES_FOR_BOSS;
        float progressFillWidth = PROGRESS_BAR_WIDTH * progress;
        shapes.setColor(0.5f, 0.3f, 0.8f, 1f);
        shapes.rect(progressX, progressY, progressFillWidth, PROGRESS_BAR_HEIGHT);

        // Progress bar border
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(progressX, progressY, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapes.end();

        // Boxes count text
        batch.begin();
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        String boxesText = boxesCollected + " / " + BOXES_FOR_BOSS;
        font.draw(batch, boxesText, WORLD_W / 2f - 25f, PROGRESS_BAR_PADDING + PROGRESS_BAR_HEIGHT + 15f);
        batch.end();
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        player.dispose();
        enemyFactory.dispose();
        projectileFactory.dispose();
        itemFactory.dispose();
        floatingTextRenderSystem.dispose();
    }
}

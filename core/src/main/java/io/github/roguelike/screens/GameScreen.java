package io.github.roguelike.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.GdxGame;
import io.github.roguelike.ecs.enemy.EnemyFactory;
import io.github.roguelike.ecs.projectile.ProjectileFactory;
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

    // Difficulty multiplier (increases by 3x each boss kill)
    private float difficultyMultiplier = 1f;

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Player player = new Player(WORLD_W / 2f, WORLD_H / 2f);

    // Ashley
    private final Engine engine = new Engine();
    private final EnemyFactory enemyFactory = new EnemyFactory();
    private final EnemyRenderSystem enemyRenderSystem = new EnemyRenderSystem(batch);
    private final SlashRenderSystem slashRenderSystem = new SlashRenderSystem(batch);
    private final ProjectileFactory projectileFactory = new ProjectileFactory();
    private final ProjectileRenderSystem projectileRenderSystem = new ProjectileRenderSystem(batch);
    private final ItemRenderSystem itemRenderSystem = new ItemRenderSystem(batch);
    private final FloatingTextRenderSystem floatingTextRenderSystem = new FloatingTextRenderSystem(batch);
    private final FlaskRenderSystem flaskRenderSystem = new FlaskRenderSystem(batch);

    public GameScreen(GdxGame game) {
        this.game = game;

        engine.addSystem(new PlayerShootSystem(player, projectileFactory)); // puščice -> nov projectile
        engine.addSystem(new EnemyStateSystem(player)); // state machine -> nastavi velocity
        engine.addSystem(new MovementSystem());         // velocity -> position
        engine.addSystem(new FacingSystem());           // velocity -> smer gledanja
        engine.addSystem(new EnemyDamageSystem(player)); // slash collision -> player damage
        engine.addSystem(new ProjectileDamageSystem(this)); // projectile collision -> enemy damage
        engine.addSystem(new ItemCollectionSystem(player)); // item collision -> stat boost
        engine.addSystem(new FlaskCollectionSystem(player)); // flask collision -> heal
        engine.addSystem(new ProjectileSystem(WORLD_W, WORLD_H)); // odstrani projectile izven sveta
        engine.addSystem(enemyRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(slashRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(projectileRenderSystem);       // processing = false, riše se ročno v render()
        engine.addSystem(itemRenderSystem);             // processing = false, riše se ročno v render()
        engine.addSystem(flaskRenderSystem);            // processing = false, riše se ročno v render()
        engine.addSystem(floatingTextRenderSystem);     // processing = false, riše se ročno v render()

    }

    public void onBossDeath() {
        difficultyMultiplier *= 3f;
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
        flaskRenderSystem.update(delta);
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
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        player.dispose();
        enemyFactory.dispose();
        projectileFactory.dispose();
        floatingTextRenderSystem.dispose();
    }
}

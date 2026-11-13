package io.github.roguelike.ecs.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.roguelike.ecs.component.*;
import io.github.roguelike.ecs.component.StatsComponent;
import io.github.roguelike.entity.Player.Direction;

import java.util.EnumMap;
import java.util.Map;

/** Ustvarja basic melee enemyje. Texture naloži enkrat in ga deli med vsemi enemyji. */
public class EnemyFactory implements Disposable {
    private static final int FRAME_SIZE = 48;
    private static final int FRAMES_PER_DIRECTION = 2;

    // boss (osnova je elite enemy, ki je že označen z isBoss)
    private static final float BOSS_SIZE_MULTIPLIER = 2f;
    private static final float BOSS_BASE_HP = 300f;
    private static final float BOSS_BASE_DAMAGE = 10f;
    private static final float BOSS_SPEED = 70f;
    private static final float BOSS_ATTACK_RANGE = 56f; // večji enemy, zato napade z večje razdalje (središče-središče)

    // slash efekt: 10 sličic po 48x24, narisan v desno
    private static final int SLASH_FRAME_W = 48;
    private static final int SLASH_FRAME_H = 24;
    private static final float SLASH_FRAME_DURATION = 0.04f;

    private final Texture idleSheet;
    private final Texture slashSheet;
    private final Animation<TextureRegion> slashAnimation;
    private final Map<Direction, Animation<TextureRegion>> idleAnimations = new EnumMap<>(Direction.class);

    public EnemyFactory() {
        idleSheet = new Texture(Gdx.files.internal("EnemySprites/TempEnemyPlaceholder.png"));
        idleSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Handle single sprite or multi-frame spritesheet
        TextureRegion[][] split = TextureRegion.split(idleSheet, FRAME_SIZE, FRAME_SIZE);
        TextureRegion[] row = split[0];
        boolean isSingleSprite = (split.length == 1 && row.length == 1);

        for (Direction d : Direction.values()) {
            TextureRegion[] frames;
            if (isSingleSprite) {
                frames = new TextureRegion[]{row[0]};
            } else {
                frames = new TextureRegion[FRAMES_PER_DIRECTION];
                System.arraycopy(row, d.ordinal() * FRAMES_PER_DIRECTION, frames, 0, FRAMES_PER_DIRECTION);
            }

            Animation<TextureRegion> anim = new Animation<>(0.3f, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            idleAnimations.put(d, anim);
        }

        slashSheet = new Texture(Gdx.files.internal("EffectsSprites/slash.png"));
        slashSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[] slashFrames = TextureRegion.split(slashSheet, SLASH_FRAME_W, SLASH_FRAME_H)[0];
        slashAnimation = new Animation<>(SLASH_FRAME_DURATION, slashFrames);
        slashAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public Entity createMeleeEnemy(Engine engine, float x, float y, float difficultyMultiplier) {
        Entity enemy = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y);
        transform.width = FRAME_SIZE;
        transform.height = FRAME_SIZE;

        // za zdaj ima enemy samo idle sprite sheet, zato ga uporabimo za obe stanji
        // (ko bo walk sheet, ga samo vstavi pod EnemyState.CHASE)
        AnimationComponent animation = new AnimationComponent();
        animation.animations.put(EnemyState.IDLE, idleAnimations);
        animation.animations.put(EnemyState.CHASE, idleAnimations);
        animation.animations.put(EnemyState.ATTACK, idleAnimations); // telo; slash se riše posebej

        AttackComponent attack = new AttackComponent();
        attack.slash = slashAnimation;

        StatsComponent stats = new StatsComponent();
        // Default values scaled by difficulty multiplier
        stats.maxHp = 30f * difficultyMultiplier;
        stats.currentHp = 30f * difficultyMultiplier;
        stats.damage = 5f * difficultyMultiplier;
        stats.attackRate = 0.5f;
        stats.movementSpeed = 100f;

        EnemyStateComponent state = new EnemyStateComponent();
        state.fsm = new DefaultStateMachine<>(enemy, EnemyState.IDLE);

        enemy.add(transform)
            .add(new VelocityComponent())
            .add(new FacingComponent())
            .add(new TargetComponent())
            .add(new EnemyComponent())
            .add(attack)
            .add(stats)
            .add(new StatusEffectsComponent())
            .add(animation)
            .add(state);

        engine.addEntity(enemy);
        return enemy;
    }

    // Overload for backward compatibility (default multiplier = 1)
    public Entity createMeleeEnemy(Engine engine, float x, float y) {
        return createMeleeEnemy(engine, x, y, 1f);
    }

    public Entity createEliteEnemy(Engine engine, float x, float y, float sizeMultiplier, float statMultiplier) {
        Entity enemy = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.position.set(x, y);
        transform.width = FRAME_SIZE * sizeMultiplier;
        transform.height = FRAME_SIZE * sizeMultiplier;

        AnimationComponent animation = new AnimationComponent();
        animation.animations.put(EnemyState.IDLE, idleAnimations);
        animation.animations.put(EnemyState.CHASE, idleAnimations);
        animation.animations.put(EnemyState.ATTACK, idleAnimations);

        AttackComponent attack = new AttackComponent();
        attack.slash = slashAnimation;

        StatsComponent stats = new StatsComponent();
        stats.maxHp = 30f * statMultiplier;
        stats.currentHp = 30f * statMultiplier;
        stats.damage = 5f * statMultiplier;
        stats.attackRate = 0.5f;
        stats.movementSpeed = 100f;
        stats.isBoss = true;

        EnemyStateComponent state = new EnemyStateComponent();
        state.fsm = new DefaultStateMachine<>(enemy, EnemyState.IDLE);

        enemy.add(transform)
            .add(new VelocityComponent())
            .add(new FacingComponent())
            .add(new TargetComponent())
            .add(new EnemyComponent())
            .add(attack)
            .add(stats)
            .add(new StatusEffectsComponent())
            .add(animation)
            .add(state);

        engine.addEntity(enemy);
        return enemy;
    }

    /**
     * Boss: dvakrat večji enemy z veliko HP, počasnejši, a z daljšim dosegom. Pojavi se tako,
     * da je njegovo središče v (centerX, centerY). HP in damage rasteta z difficultyMultiplier.
     */
    public Entity createBossEnemy(Engine engine, float centerX, float centerY, float difficultyMultiplier) {
        Entity boss = createEliteEnemy(engine, 0f, 0f, BOSS_SIZE_MULTIPLIER, difficultyMultiplier);

        TransformComponent transform = Mappers.TRANSFORM.get(boss);
        transform.position.set(centerX - transform.width / 2f, centerY - transform.height / 2f);

        StatsComponent stats = Mappers.STATS.get(boss);
        stats.maxHp = BOSS_BASE_HP * difficultyMultiplier;
        stats.currentHp = stats.maxHp;
        stats.damage = BOSS_BASE_DAMAGE * difficultyMultiplier;
        stats.movementSpeed = BOSS_SPEED;

        Mappers.ENEMY.get(boss).speed = BOSS_SPEED;
        Mappers.ATTACK.get(boss).range = BOSS_ATTACK_RANGE;
        return boss;
    }

    @Override
    public void dispose() {
        idleSheet.dispose();
        slashSheet.dispose();
    }
}

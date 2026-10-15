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
import io.github.roguelike.entity.Player.Direction;

import java.util.EnumMap;
import java.util.Map;

/** Ustvarja basic melee enemyje. Texture naloži enkrat in ga deli med vsemi enemyji. */
public class EnemyFactory implements Disposable {
    private static final int FRAME_SIZE = 48;
    private static final int FRAMES_PER_DIRECTION = 2;

    // slash efekt: 10 sličic po 48x24, narisan v desno
    private static final int SLASH_FRAME_W = 48;
    private static final int SLASH_FRAME_H = 24;
    private static final float SLASH_FRAME_DURATION = 0.04f;

    private final Texture idleSheet;
    private final Texture slashSheet;
    private final Animation<TextureRegion> slashAnimation;
    private final Map<Direction, Animation<TextureRegion>> idleAnimations = new EnumMap<>(Direction.class);

    public EnemyFactory() {
        idleSheet = new Texture(Gdx.files.internal("EnemySprites/EnemyIdleTemp.png"));
        idleSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // enak vrstni red kot pri playerju: DOWN, UP, RIGHT, LEFT (vsak 2 sličici)
        TextureRegion[] row = TextureRegion.split(idleSheet, FRAME_SIZE, FRAME_SIZE)[0];
        for (Direction d : Direction.values()) {
            TextureRegion[] frames = new TextureRegion[FRAMES_PER_DIRECTION];
            System.arraycopy(row, d.ordinal() * FRAMES_PER_DIRECTION, frames, 0, FRAMES_PER_DIRECTION);

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

    public Entity createMeleeEnemy(Engine engine, float x, float y) {
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

        EnemyStateComponent state = new EnemyStateComponent();
        state.fsm = new DefaultStateMachine<>(enemy, EnemyState.IDLE);

        enemy.add(transform)
            .add(new VelocityComponent())
            .add(new FacingComponent())
            .add(new TargetComponent())
            .add(new EnemyComponent())
            .add(attack)
            .add(animation)
            .add(state);

        engine.addEntity(enemy);
        return enemy;
    }

    @Override
    public void dispose() {
        idleSheet.dispose();
        slashSheet.dispose();
    }
}

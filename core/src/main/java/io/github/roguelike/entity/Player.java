package io.github.roguelike.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.EnumMap;
import java.util.Map;

public class Player extends Entity {
    private static final int FRAME_SIZE = 48;
    private static final int FRAMES_PER_DIRECTION = 2;
    private static final int WALK_FRAMES_PER_DIRECTION = 6;

    // vrstni red smeri v walk spritesheetu (drugačen kot pri idle!): gor, dol, desno, levo
    private static final Direction[] WALK_SHEET_ORDER = {
        Direction.UP, Direction.DOWN, Direction.RIGHT, Direction.LEFT
    };

    private final Map<Direction, Animation<TextureRegion>> idleAnimations = new EnumMap<>(Direction.class);
    private final Map<Direction, Animation<TextureRegion>> walkAnimations = new EnumMap<>(Direction.class);
    private final Array<Texture> textures = new Array<>();

    private PlayerState state = PlayerState.IDLE;
    private Direction facing = Direction.DOWN;
    private float stateTime = 0f;

    // Stats
    private float maxHp;
    private float currentHp;
    private float damage;
    private float attackRate;
    private float movementSpeed;

    public enum PlayerState {
        IDLE,
        WALK
        // kasneje: ATTACK1, ATTACK2, ATTACK3, MAGIC1, MAGIC2, HURT, DEAD ...
    }

    // vrstni red je enak kot v spritesheetu (vsak ima 2 sličici)
    public enum Direction {
        DOWN(0), UP(1), RIGHT(2), LEFT(3);

        final int startFrame;
        Direction(int index) { this.startFrame = index * FRAMES_PER_DIRECTION; }
    }

    public Player(float x, float y) {
        super(x, y, FRAME_SIZE, FRAME_SIZE);
        speed = 250f;

        // Initialize stats with default values
        this.maxHp = 50f;
        this.currentHp = 50f;
        this.damage = 10f;
        this.attackRate = 1f;
        this.movementSpeed = 250f;

        loadIdle("PlayerSprites/WizardIdleOutline.png", 0.3f);
        loadWalk("PlayerSprites/WizardWalkOutline.png", 0.1f);
    }

    private Texture loadTexture(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(tex);
        return tex;
    }

    private void loadIdle(String path, float frameDuration) {
        TextureRegion[] row = TextureRegion.split(loadTexture(path), FRAME_SIZE, FRAME_SIZE)[0];

        for (Direction d : Direction.values()) {
            TextureRegion[] frames = new TextureRegion[FRAMES_PER_DIRECTION];
            System.arraycopy(row, d.startFrame, frames, 0, FRAMES_PER_DIRECTION);

            Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            idleAnimations.put(d, anim);
        }
    }

    private void loadWalk(String path, float frameDuration) {
        TextureRegion[] row = TextureRegion.split(loadTexture(path), FRAME_SIZE, FRAME_SIZE)[0];

        // 4 smeri x 6 sličic, v vrstnem redu WALK_SHEET_ORDER
        for (int i = 0; i < WALK_SHEET_ORDER.length; i++) {
            TextureRegion[] frames = new TextureRegion[WALK_FRAMES_PER_DIRECTION];
            System.arraycopy(row, i * WALK_FRAMES_PER_DIRECTION, frames, 0, WALK_FRAMES_PER_DIRECTION);

            Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            walkAnimations.put(WALK_SHEET_ORDER[i], anim);
        }
    }

    private void setState(PlayerState newState) {
        if (state == newState) return;
        state = newState;
        stateTime = 0f;
    }

    @Override
    public void update(float delta) {
        handleInput();
        position.mulAdd(velocity, delta);
        updateFacing();
        updateState();
        stateTime += delta;
    }

    private void handleInput() {
        velocity.set(0, 0);
        // samo WASD; puščične tipke so za streljanje (PlayerShootSystem)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x += 1;
        velocity.nor().scl(speed);
    }

    // zapomni si smer zadnjega premikanja (pri diagonali zmaga horizontala)
    private void updateFacing() {
        if (velocity.isZero()) return;
        if (Math.abs(velocity.x) >= Math.abs(velocity.y)) {
            facing = velocity.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = velocity.y > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    private void updateState() {
        switch (state) {
            case IDLE:
                if (!velocity.isZero()) setState(PlayerState.WALK);
                break;
            case WALK:
                if (velocity.isZero()) setState(PlayerState.IDLE);
                break;
        }
    }

    public void clampToWorld(float worldW, float worldH) {
        position.x = MathUtils.clamp(position.x, 0, worldW - width);
        position.y = MathUtils.clamp(position.y, 0, worldH - height);
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> anim = (state == PlayerState.IDLE)
            ? idleAnimations.get(facing)
            : walkAnimations.get(facing);

        batch.draw(anim.getKeyFrame(stateTime), position.x, position.y, width, height);
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
    }

    // Stats getters and setters
    public float getMaxHp() { return maxHp; }
    public void setMaxHp(float maxHp) { this.maxHp = maxHp; }

    public float getCurrentHp() { return currentHp; }
    public void setCurrentHp(float currentHp) { this.currentHp = currentHp; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getAttackRate() { return attackRate; }
    public void setAttackRate(float attackRate) { this.attackRate = attackRate; }

    public float getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(float movementSpeed) { this.movementSpeed = movementSpeed; }
}

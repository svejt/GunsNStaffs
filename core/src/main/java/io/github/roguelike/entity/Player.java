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

    private final Map<PlayerState, Animation<TextureRegion>> animations = new EnumMap<>(PlayerState.class);
    private final Map<Direction, Animation<TextureRegion>> idleAnimations = new EnumMap<>(Direction.class);
    private final Array<Texture> textures = new Array<>();

    private PlayerState state = PlayerState.IDLE;
    private Direction facing = Direction.DOWN;
    private float stateTime = 0f;

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

        loadIdle("PlayerSprites/WizardIdle.png", 0.3f);
        loadWalk("PlayerSprites/walk_sprite_sheet_template.png", 1); // za zdaj 1 sličica
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

    private void loadWalk(String path, int frameCount) {
        TextureRegion[] row = TextureRegion.split(loadTexture(path), FRAME_SIZE, FRAME_SIZE)[0];
        TextureRegion[] used = new TextureRegion[frameCount];
        System.arraycopy(row, 0, used, 0, frameCount);

        Animation<TextureRegion> anim = new Animation<>(0.1f, used);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        animations.put(PlayerState.WALK, anim);
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
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    velocity.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  velocity.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  velocity.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocity.x += 1;
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
            : animations.get(state);

        batch.draw(anim.getKeyFrame(stateTime), position.x, position.y, width, height);
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
    }
}

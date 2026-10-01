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

    private final Map<PlayerState, Animation<TextureRegion>> animations = new EnumMap<>(PlayerState.class);
    private final Array<Texture> textures = new Array<>();

    private PlayerState state = PlayerState.IDLE;
    private float stateTime = 0f;


    public enum PlayerState {
        IDLE,
        WALK
        // kasneje: ATTACK1, ATTACK2, ATTACK3, MAGIC1, MAGIC2, HURT, DEAD ...
    }

    public Player(float x, float y) {
        super(x, y, FRAME_SIZE, FRAME_SIZE);
        speed = 250f;

        // frameCount = 1 pomeni samo prva sličica. Ko boš hotel animacijo, daj 8.
        load(PlayerState.IDLE, "PlayerSprites/Idle_sprite_sheet_template.png", 1);
        load(PlayerState.WALK, "PlayerSprites/walk_sprite_sheet_template.png", 1);
    }

    private void load(PlayerState s, String path, int frameCount) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(tex);

        TextureRegion[] row = TextureRegion.split(tex, FRAME_SIZE, FRAME_SIZE)[0];
        TextureRegion[] used = new TextureRegion[frameCount];
        System.arraycopy(row, 0, used, 0, frameCount);

        Animation<TextureRegion> anim = new Animation<>(0.1f, used);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        animations.put(s, anim);
    }

    private void setState(PlayerState newState) {
        if (state == newState) return;   // isto stanje: ne resetiraj časa
        state = newState;
        stateTime = 0f;
    }

    @Override
    public void update(float delta) {
        handleInput();
        position.mulAdd(velocity, delta);
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

    // Tu se odloča, v katero stanje preideš. Vse prehode imaš na enem mestu.
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
        TextureRegion frame = animations.get(state).getKeyFrame(stateTime);
        batch.draw(frame, position.x, position.y, width, height);
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
    }
}

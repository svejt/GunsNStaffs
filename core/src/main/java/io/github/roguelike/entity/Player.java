package io.github.roguelike.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.roguelike.weapon.WeaponType;

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

    // Orožje: loadout[i] je na tipki (i + 1); 1-3 = puške, 4-7 = staffi (voda, ogenj, zemlja, veter)
    private static final WeaponType[] DEFAULT_LOADOUT = {
        WeaponType.PISTOL, WeaponType.RIFLE, WeaponType.SHOTGUN,
        WeaponType.WATER_STAFF, WeaponType.FIRE_STAFF, WeaponType.EARTH_STAFF, WeaponType.WIND_STAFF
    };
    private static final int[] SLOT_KEYS = {
        Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4,
        Input.Keys.NUM_5, Input.Keys.NUM_6, Input.Keys.NUM_7
    };
    private static final int[] SLOT_KEYS_NUMPAD = {
        Input.Keys.NUMPAD_1, Input.Keys.NUMPAD_2, Input.Keys.NUMPAD_3, Input.Keys.NUMPAD_4,
        Input.Keys.NUMPAD_5, Input.Keys.NUMPAD_6, Input.Keys.NUMPAD_7
    };
    private static final float HAND_OFFSET_Y = -4f; // pivot orožja glede na središče playerja
    private static final float HOLD_DISTANCE = 4f;  // pivot orožja se od središča premakne v smer ciljanja

    private final WeaponType[] loadout = DEFAULT_LOADOUT.clone();
    private final Map<WeaponType, TextureRegion> weaponRegions = new EnumMap<>(WeaponType.class);
    private int activeSlot = 0;

    // Ciljanje: aiming = true, dokler player strelja (nastavlja PlayerShootSystem)
    private boolean aiming = false;
    private final Vector2 aimDirection = new Vector2(1, 0);

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

        /** Najbližja od štirih smeri za dani vektor (pri diagonali zmaga horizontala). */
        public static Direction fromVector(float x, float y) {
            if (Math.abs(x) >= Math.abs(y)) return x > 0 ? RIGHT : LEFT;
            return y > 0 ? UP : DOWN;
        }

        /** Kot smeri v stopinjah (desno = 0, gor = 90). */
        public float angleDeg() {
            switch (this) {
                case UP:   return 90f;
                case LEFT: return 180f;
                case DOWN: return 270f;
                default:   return 0f;
            }
        }
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
        loadWeapons();
    }

    private void loadWeapons() {
        for (WeaponType type : WeaponType.values()) {
            weaponRegions.put(type, new TextureRegion(loadTexture(type.texturePath)));
        }
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
        // preklop orožja s tipkami 1-3
        for (int i = 0; i < loadout.length; i++) {
            if (Gdx.input.isKeyJustPressed(SLOT_KEYS[i]) || Gdx.input.isKeyJustPressed(SLOT_KEYS_NUMPAD[i])) {
                selectSlot(i);
            }
        }

        velocity.set(0, 0);
        // samo WASD; puščične tipke so za streljanje (PlayerShootSystem)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x += 1;
        velocity.nor().scl(speed);
    }

    // zapomni si smer zadnjega premikanja (pri diagonali zmaga horizontala);
    // med streljanjem ima prednost smer ciljanja (glej aimAt)
    private void updateFacing() {
        if (aiming || velocity.isZero()) return;
        facing = Direction.fromVector(velocity.x, velocity.y);
    }

    /** Izbere orožje v danem slotu (0-based); neveljavni indeksi se ignorirajo. */
    public void selectSlot(int slot) {
        if (slot < 0 || slot >= loadout.length) return;
        activeSlot = slot;
    }

    public int getActiveSlot() { return activeSlot; }
    public WeaponType[] getLoadout() { return loadout; }
    public WeaponType getActiveWeapon() { return loadout[activeSlot]; }
    public TextureRegion getWeaponRegion(WeaponType type) { return weaponRegions.get(type); }

    /** Player strelja v smeri direction: gleda in cilja v to smer, ne glede na smer hoje. */
    public void aimAt(Vector2 direction) {
        if (direction.isZero()) return;
        aiming = true;
        aimDirection.set(direction).nor();
        facing = Direction.fromVector(aimDirection.x, aimDirection.y);
    }

    /** Player ne strelja več: facing se spet ravna po hoji. */
    public void stopAiming() {
        aiming = false;
    }

    public boolean isAiming() { return aiming; }

    /** Kot orožja v stopinjah: smer ciljanja med streljanjem, sicer smer gledanja. */
    public float getGunAngleDeg() {
        return aiming ? aimDirection.angleDeg() : facing.angleDeg();
    }

    private Vector2 getGunPivot(Vector2 out) {
        float angle = getGunAngleDeg() * MathUtils.degreesToRadians;
        return out.set(position.x + width / 2f + MathUtils.cos(angle) * HOLD_DISTANCE,
                       position.y + height / 2f + HAND_OFFSET_Y + MathUtils.sin(angle) * HOLD_DISTANCE);
    }

    /** Konica cevi aktivnega orožja v svetovnih koordinatah. */
    public Vector2 getMuzzlePosition(Vector2 out) {
        float angle = getGunAngleDeg() * MathUtils.degreesToRadians;
        getGunPivot(out);
        return out.add(MathUtils.cos(angle) * getActiveWeapon().muzzleLength,
                       MathUtils.sin(angle) * getActiveWeapon().muzzleLength);
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

        // proti gor je orožje za telesom, sicer pred njim
        if (facing == Direction.UP) renderWeapon(batch);
        batch.draw(anim.getKeyFrame(stateTime), position.x, position.y, width, height);
        if (facing != Direction.UP) renderWeapon(batch);
    }

    private final Vector2 gunPivot = new Vector2();

    /** Riše aktivno orožje (puško ali staff) v roki, zavrteno v smer ciljanja / gledanja. */
    private void renderWeapon(SpriteBatch batch) {
        WeaponType weapon = getActiveWeapon();
        float angle = getGunAngleDeg();
        // ko orožje kaže v levo, ga prezrcalimo, da ni obrnjeno na glavo
        float flipY = (angle > 90f && angle < 270f) ? -1f : 1f;

        getGunPivot(gunPivot);
        batch.draw(weaponRegions.get(weapon),
            gunPivot.x - weapon.gripX, gunPivot.y - weapon.gripY,
            weapon.gripX, weapon.gripY,
            48f, 48f,
            1f, flipY,
            angle);
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

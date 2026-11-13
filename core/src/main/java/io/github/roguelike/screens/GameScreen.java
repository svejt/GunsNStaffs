package io.github.roguelike.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.roguelike.GdxGame;
import io.github.roguelike.ecs.component.EnemyComponent;
import io.github.roguelike.ecs.component.Mappers;
import io.github.roguelike.ecs.component.ProjectileComponent;
import io.github.roguelike.ecs.component.StatsComponent;
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
import io.github.roguelike.ecs.system.StatusEffectSystem;
import io.github.roguelike.entity.Player;
import io.github.roguelike.ui.BossHealthBar;
import io.github.roguelike.weapon.WeaponType;
import io.github.roguelike.world.Dungeon;
import io.github.roguelike.world.Room;
import io.github.roguelike.world.Side;

import java.util.Random;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_W = 640, WORLD_H = 480; // 4:3

    // Room settings
    private static final int MIN_ROOMS = 7, MAX_ROOMS = 12;
    private static final float WALL_THICKNESS = 12f;
    private static final float DOOR_SIZE = 96f;     // širina odprtine v steni
    private static final float EDGE_TOLERANCE = 1f; // kako blizu roba mora biti igralec, da sproži vrata
    private static final float ENTRY_OFFSET = 24f;  // kje se igralec pojavi v novi sobi (od roba)
    private static final float LABEL_SCALE = 8f;

    // Enemy spawn settings (ob prvem vstopu v sobo, razen v začetni)
    private static final int MIN_ENEMIES = 5, MAX_ENEMIES = 10;
    private static final float ENEMY_SIZE = 48f;           // enako kot FRAME_SIZE v EnemyFactory
    private static final float ENEMY_SPAWN_MARGIN = 24f;   // razmik od sten
    private static final float ENEMY_MIN_DISTANCE = 160f;  // najmanjša razdalja od igralca ob spawnu

    // Minimap settings
    private static final float MAP_PANEL_W = 160f, MAP_PANEL_H = 120f; // 4:3, kot zaslon
    private static final float MAP_MARGIN = 10f;   // razmik od roba zaslona
    private static final float MAP_PADDING = 10f;  // razmik od roba panela do sob
    private static final float MAP_MAX_STEP = 32f; // da majhne postavitve ne postanejo ogromne
    private static final float MAP_ROOM_FILL = 0.7f; // delež koraka, ki ga zavzame soba

    // Health bar settings
    private static final float HP_BAR_WIDTH = 150f;
    private static final float HP_BAR_HEIGHT = 15f;
    private static final float HP_BAR_PADDING = 10f;

    // Weapon HUD settings (spodaj levo)
    private static final float SLOT_SIZE = 36f; // 7 slotov (3 puške + 4 staffi)
    private static final float SLOT_GAP = 4f;
    private static final float SLOT_MARGIN = 10f;

    // Boss settings
    private static final String BOSS_NAME = "BOSS";
    private static final float BOSS_BAR_Y = SLOT_MARGIN + SLOT_SIZE + 10f; // nad vrsto orožja, spodaj na sredini

    // Difficulty multiplier (increases by 3x each boss kill)
    private float difficultyMultiplier = 1f;

    private final GdxGame game;
    private final FitViewport viewport = new FitViewport(WORLD_W, WORLD_H);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Player player = new Player(WORLD_W / 2f, WORLD_H / 2f);

    // Rooms
    private final Dungeon dungeon = Dungeon.generate(new Random(), MIN_ROOMS, MAX_ROOMS);
    private Room currentRoom = dungeon.getStart();
    private final BitmapFont roomFont = new BitmapFont();
    private final GlyphLayout roomLabelLayout = new GlyphLayout();
    private final BitmapFont hudFont = new BitmapFont();
    private final GlyphLayout hudLayout = new GlyphLayout();
    private final Random random = new Random();
    private final Family enemyFamily = Family.all(EnemyComponent.class).get();

    // Boss encounter: boss se pojavi ob vstopu v boss sobo, bar pa sledi njegovemu HP
    private final BossHealthBar bossHealthBar = new BossHealthBar();
    private Entity boss;

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

        // velika, polprosojna črka sobe na sredini (nearest filter, da ostane ostra)
        roomFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        roomFont.getData().setScale(LABEL_SCALE);
        roomFont.setColor(1f, 1f, 1f, 0.2f);

        engine.addSystem(new PlayerShootSystem(player, projectileFactory, viewport)); // puščice/miška -> projectile aktivnega orožja
        engine.addSystem(new EnemyStateSystem(player)); // state machine -> nastavi velocity
        engine.addSystem(new StatusEffectSystem(this, WORLD_W, WORLD_H, WALL_THICKNESS)); // burn, slow/stun timerji, knockback
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

    /** Stran vrat (ki vodijo v sosednjo sobo), ob katerih igralec stoji, ne glede na zaklepe; sicer null. */
    private Side doorAtPlayer() {
        Rectangle b = player.getBounds();
        float centerX = b.x + b.width / 2f;
        float centerY = b.y + b.height / 2f;
        boolean alignedWithVerticalDoor = Math.abs(centerX - WORLD_W / 2f) <= DOOR_SIZE / 2f;
        boolean alignedWithHorizontalDoor = Math.abs(centerY - WORLD_H / 2f) <= DOOR_SIZE / 2f;

        if (alignedWithVerticalDoor && b.y + b.height >= WORLD_H - EDGE_TOLERANCE
            && dungeon.hasDoor(currentRoom, Side.UP)) return Side.UP;
        if (alignedWithVerticalDoor && b.y <= EDGE_TOLERANCE
            && dungeon.hasDoor(currentRoom, Side.DOWN)) return Side.DOWN;
        if (alignedWithHorizontalDoor && b.x <= EDGE_TOLERANCE
            && dungeon.hasDoor(currentRoom, Side.LEFT)) return Side.LEFT;
        if (alignedWithHorizontalDoor && b.x + b.width >= WORLD_W - EDGE_TOLERANCE
            && dungeon.hasDoor(currentRoom, Side.RIGHT)) return Side.RIGHT;
        return null;
    }

    /** Vrne stran vrat, skozi katera igralec lahko preide (dotika se jih, so odklenjena), sicer null. */
    private Side findTouchedDoor() {
        // dokler v sobi ostanejo enemyji, so vrata zaklenjena
        if (!currentRoom.isCleared()) return null;

        Side side = doorAtPlayer();
        if (side == null) return null;
        // v boss sobo se vstopi šele, ko so vse ostale sobe cleared
        if (dungeon.isDoorToLockedBossRoom(currentRoom, side)) return null;
        return side;
    }

    /** Premakne igralca skozi vrata na dani strani v sosednjo sobo (pojavi se ob nasprotnih vratih). */
    private void enterNeighborRoom(Side side) {
        currentRoom = dungeon.getNeighbor(currentRoom, side);

        Rectangle b = player.getBounds();
        switch (side) {
            case UP:    player.setPosition(b.x, ENTRY_OFFSET); break;
            case DOWN:  player.setPosition(b.x, WORLD_H - b.height - ENTRY_OFFSET); break;
            case LEFT:  player.setPosition(WORLD_W - b.width - ENTRY_OFFSET, b.y); break;
            case RIGHT: player.setPosition(ENTRY_OFFSET, b.y); break;
        }

        // izstreljeni projektili ne smejo "potovati" v drugo sobo
        ImmutableArray<Entity> projectiles = engine.getEntitiesFor(Family.all(ProjectileComponent.class).get());
        Array<Entity> toRemove = new Array<>();
        for (Entity projectile : projectiles) toRemove.add(projectile);
        for (Entity projectile : toRemove) engine.removeEntity(projectile);

        // nepočiščena soba: ob vstopu se pojavijo enemyji (ker je ne moreš zapustiti, dokler ni cleared,
        // se to zgodi samo enkrat na sobo)
        if (!currentRoom.isCleared()) {
            if (currentRoom.isBoss()) spawnBoss();
            else spawnEnemies();
        }
    }

    /** Boss se pojavi natančno na sredini boss sobe, prikaže se njegov health bar. */
    private void spawnBoss() {
        boss = enemyFactory.createBossEnemy(engine, WORLD_W / 2f, WORLD_H / 2f, difficultyMultiplier);
        bossHealthBar.show(BOSS_NAME, Mappers.STATS.get(boss).maxHp);
    }

    /** Health bar sledi HP-ju bossa; ko boss pade, se bar skrije. */
    private void updateBossHealthBar(float delta) {
        if (boss != null) {
            StatsComponent stats = Mappers.STATS.get(boss);
            if (stats == null || stats.currentHp <= 0f) {
                bossHealthBar.setHealth(0f);
                bossHealthBar.hide();
                boss = null;
            } else {
                bossHealthBar.setHealth(stats.currentHp);
            }
        }
        bossHealthBar.update(delta);
    }

    /** Pojavi naključno število enemyjev na naključnih mestih v sobi, vsaj ENEMY_MIN_DISTANCE stran od igralca. */
    private void spawnEnemies() {
        int count = MIN_ENEMIES + random.nextInt(MAX_ENEMIES - MIN_ENEMIES + 1);

        Rectangle pb = player.getBounds();
        float playerX = pb.x + pb.width / 2f;
        float playerY = pb.y + pb.height / 2f;

        float minX = WALL_THICKNESS + ENEMY_SPAWN_MARGIN;
        float maxX = WORLD_W - WALL_THICKNESS - ENEMY_SPAWN_MARGIN - ENEMY_SIZE;
        float minY = WALL_THICKNESS + ENEMY_SPAWN_MARGIN;
        float maxY = WORLD_H - WALL_THICKNESS - ENEMY_SPAWN_MARGIN - ENEMY_SIZE;

        for (int i = 0; i < count; i++) {
            float x = minX, y = minY;
            // nekaj poskusov, da enemy ne nastane tik ob igralcu (če nobeden ne uspe, velja zadnji)
            for (int attempt = 0; attempt < 20; attempt++) {
                x = minX + random.nextFloat() * (maxX - minX);
                y = minY + random.nextFloat() * (maxY - minY);
                float dx = x + ENEMY_SIZE / 2f - playerX;
                float dy = y + ENEMY_SIZE / 2f - playerY;
                if (dx * dx + dy * dy >= ENEMY_MIN_DISTANCE * ENEMY_MIN_DISTANCE) break;
            }
            enemyFactory.createMeleeEnemy(engine, x, y, difficultyMultiplier);
        }
    }

    /** Če v nepočiščeni sobi ni več enemyjev, jo označi kot cleared (vrata se odklenejo). */
    private void updateRoomCleared() {
        if (currentRoom.isCleared()) return;
        if (engine.getEntitiesFor(enemyFamily).size() == 0) currentRoom.setCleared(true);
    }

    /** Nastavi barvo vrat na dani strani trenutne sobe. */
    private void setDoorColor(Side side) {
        Room neighbor = dungeon.getNeighbor(currentRoom, side);
        if (!currentRoom.isCleared()) shapes.setColor(0.7f, 0.15f, 0.15f, 1f);            // v sobi so še enemyji
        else if (neighbor != null && neighbor.isBoss()) {
            if (dungeon.isBossRoomLocked()) shapes.setColor(0.3f, 0.1f, 0.45f, 1f);       // boss soba zaklenjena
            else shapes.setColor(0.75f, 0.3f, 0.95f, 1f);                                 // boss soba odprta
        } else shapes.setColor(0.6f, 0.42f, 0.18f, 1f);                                   // navadna odprta vrata
    }

    private void drawRoom() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // tla
        shapes.setColor(0.1f, 0.1f, 0.12f, 1f);
        shapes.rect(0, 0, WORLD_W, WORLD_H);

        // stene
        shapes.setColor(0.35f, 0.35f, 0.4f, 1f);
        shapes.rect(0, 0, WORLD_W, WALL_THICKNESS);                          // spodaj
        shapes.rect(0, WORLD_H - WALL_THICKNESS, WORLD_W, WALL_THICKNESS);   // zgoraj
        shapes.rect(0, 0, WALL_THICKNESS, WORLD_H);                          // levo
        shapes.rect(WORLD_W - WALL_THICKNESS, 0, WALL_THICKNESS, WORLD_H);   // desno

        // vrata (odprtine v stenah) samo tam, kjer je sosednja soba; rdeča = zaklenjena, rjava = odprta,
        // vijolična = vrata v boss sobo (temna, dokler je zaklenjena)
        float doorX = (WORLD_W - DOOR_SIZE) / 2f;
        float doorY = (WORLD_H - DOOR_SIZE) / 2f;
        if (dungeon.hasDoor(currentRoom, Side.UP)) {
            setDoorColor(Side.UP);
            shapes.rect(doorX, WORLD_H - WALL_THICKNESS, DOOR_SIZE, WALL_THICKNESS);
        }
        if (dungeon.hasDoor(currentRoom, Side.DOWN)) {
            setDoorColor(Side.DOWN);
            shapes.rect(doorX, 0, DOOR_SIZE, WALL_THICKNESS);
        }
        if (dungeon.hasDoor(currentRoom, Side.LEFT)) {
            setDoorColor(Side.LEFT);
            shapes.rect(0, doorY, WALL_THICKNESS, DOOR_SIZE);
        }
        if (dungeon.hasDoor(currentRoom, Side.RIGHT)) {
            setDoorColor(Side.RIGHT);
            shapes.rect(WORLD_W - WALL_THICKNESS, doorY, WALL_THICKNESS, DOOR_SIZE);
        }

        shapes.end();
    }

    @Override
    public void render(float delta) {
        player.update(delta);

        // vrata preverimo pred clampToWorld, ker ta igralca sicer ustavi na robu
        Side touchedDoor = findTouchedDoor();
        if (touchedDoor != null) enterNeighborRoom(touchedDoor);

        player.clampToWorld(WORLD_W, WORLD_H);

        // Check for game over
        if (player.getCurrentHp() <= 0f) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        engine.update(delta);
        updateRoomCleared(); // po update-u, ker se mrtvi enemyji odstranijo šele ob koncu update-a
        updateBossHealthBar(delta);

        // celoten zaslon (tudi letterbox robovi) črn
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // pobarvaj samo območje igre (tla, stene, vrata)
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        drawRoom();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        // črka sobe je v ozadju, pod vsem ostalim
        roomLabelLayout.setText(roomFont, String.valueOf(currentRoom.getLabel()));
        roomFont.draw(batch, roomLabelLayout,
            (WORLD_W - roomLabelLayout.width) / 2f,
            (WORLD_H + roomLabelLayout.height) / 2f);
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

        drawWeaponHud();
        bossHealthBar.render(shapes, batch, viewport.getCamera().combined, WORLD_W, BOSS_BAR_Y);
        drawBossLockedHint();

        // Minimap (samo dokler je TAB pritisnjen)
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) drawMinimap();
    }

    /** Ko igralec stoji ob zaklenjenih vratih boss sobe, mu napis pove, zakaj se ne odprejo. */
    private void drawBossLockedHint() {
        if (!currentRoom.isCleared()) return;
        Side side = doorAtPlayer();
        if (side == null || !dungeon.isDoorToLockedBossRoom(currentRoom, side)) return;

        hudFont.getData().setScale(1f);
        hudFont.setColor(0.85f, 0.5f, 1f, 1f); // barvo je treba nastaviti PRED setText, ker jo GlyphLayout vzame takrat
        hudLayout.setText(hudFont, "Boss room locked - clear all other rooms");
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        hudFont.draw(batch, hudLayout, (WORLD_W - hudLayout.width) / 2f, WORLD_H - 30f);
        batch.end();
    }

    /** Sloti orožja (tipke 1-7: puške, staffi) spodaj levo; aktivni je osvetljen. */
    private void drawWeaponHud() {
        WeaponType[] loadout = player.getLoadout();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < loadout.length; i++) {
            float x = SLOT_MARGIN + i * (SLOT_SIZE + SLOT_GAP);
            if (i == player.getActiveSlot()) shapes.setColor(0.9f, 0.75f, 0.2f, 0.55f);
            else shapes.setColor(0f, 0f, 0f, 0.55f);
            shapes.rect(x, SLOT_MARGIN, SLOT_SIZE, SLOT_SIZE);
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < loadout.length; i++) {
            float x = SLOT_MARGIN + i * (SLOT_SIZE + SLOT_GAP);
            if (i == player.getActiveSlot()) shapes.setColor(1f, 0.9f, 0.4f, 1f);
            else shapes.setColor(0.6f, 0.6f, 0.6f, 1f);
            shapes.rect(x, SLOT_MARGIN, SLOT_SIZE, SLOT_SIZE);
        }
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int i = 0; i < loadout.length; i++) {
            float x = SLOT_MARGIN + i * (SLOT_SIZE + SLOT_GAP);
            TextureRegion icon = player.getWeaponRegion(loadout[i]);
            batch.draw(icon, x, SLOT_MARGIN, SLOT_SIZE, SLOT_SIZE);
            // številka tipke v zgornjem levem kotu slota
            roomFont.getData().setScale(1f);
            roomFont.setColor(1f, 1f, 1f, 1f);
            roomFont.draw(batch, String.valueOf(i + 1), x + 3f, SLOT_MARGIN + SLOT_SIZE - 3f);
        }
        // povrni nastavitve velike črke sobe
        roomFont.getData().setScale(LABEL_SCALE);
        roomFont.setColor(1f, 1f, 1f, 0.2f);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Minimap v levem zgornjem kotu: fiksen pravokotnik, postavitev sob se prilagodi, da se vedno ujame vanj. */
    private void drawMinimap() {
        float panelX = MAP_MARGIN;
        float panelY = WORLD_H - MAP_PANEL_H - MAP_MARGIN;

        // meje postavitve v mreži sob
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Room room : dungeon.getRooms()) {
            minX = Math.min(minX, room.getGridX());
            maxX = Math.max(maxX, room.getGridX());
            minY = Math.min(minY, room.getGridY());
            maxY = Math.max(maxY, room.getGridY());
        }
        int cols = maxX - minX + 1;
        int rows = maxY - minY + 1;

        // korak med sobami: soba je MAP_ROOM_FILL koraka velika, ostalo je vrzel za vrata
        float availW = MAP_PANEL_W - 2f * MAP_PADDING;
        float availH = MAP_PANEL_H - 2f * MAP_PADDING;
        float step = Math.min(MAP_MAX_STEP,
            Math.min(availW / (cols - 1 + MAP_ROOM_FILL), availH / (rows - 1 + MAP_ROOM_FILL)));
        float roomSize = step * MAP_ROOM_FILL;
        float gap = step - roomSize;
        float doorSize = roomSize * 0.3f;

        // postavitev na sredino panela
        float extentW = (cols - 1) * step + roomSize;
        float extentH = (rows - 1) * step + roomSize;
        float originX = panelX + (MAP_PANEL_W - extentW) / 2f;
        float originY = panelY + (MAP_PANEL_H - extentH) / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // ozadje panela
        shapes.setColor(0f, 0f, 0f, 0.65f);
        shapes.rect(panelX, panelY, MAP_PANEL_W, MAP_PANEL_H);

        Room bossRoom = dungeon.getBossRoom();
        float bossX = 0f, bossY = 0f;

        for (Room room : dungeon.getRooms()) {
            float rx = originX + (room.getGridX() - minX) * step;
            float ry = originY + (room.getGridY() - minY) * step;

            // vrata proti desni in navzgor (vsak par sosed samo enkrat)
            shapes.setColor(0.6f, 0.42f, 0.18f, 1f);
            if (dungeon.hasDoor(room, Side.RIGHT))
                shapes.rect(rx + roomSize, ry + (roomSize - doorSize) / 2f, gap, doorSize);
            if (dungeon.hasDoor(room, Side.UP))
                shapes.rect(rx + (roomSize - doorSize) / 2f, ry + roomSize, doorSize, gap);

            // soba: cleared = siva, nepočiščena = rdeča; boss soba je vedno vijolična
            if (room.isBoss()) {
                if (room.isCleared()) shapes.setColor(0.45f, 0.35f, 0.55f, 1f);          // boss premagan
                else if (dungeon.isBossRoomLocked()) shapes.setColor(0.5f, 0.15f, 0.7f, 1f); // zaklenjena
                else shapes.setColor(0.8f, 0.35f, 1f, 1f);                               // odklenjena
                bossX = rx;
                bossY = ry;
            } else if (room.isCleared()) shapes.setColor(0.55f, 0.55f, 0.6f, 1f);
            else shapes.setColor(0.8f, 0.25f, 0.25f, 1f);
            shapes.rect(rx, ry, roomSize, roomSize);

            // trenutna soba: bela pika na sredini (v boss sobi jo nadomesti bel rob, da črka ostane berljiva)
            if (room == currentRoom && !room.isBoss()) {
                float dot = roomSize * 0.4f;
                shapes.setColor(1f, 1f, 1f, 1f);
                shapes.rect(rx + (roomSize - dot) / 2f, ry + (roomSize - dot) / 2f, dot, dot);
            }
        }
        shapes.end();

        // obroba panela
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 1f);
        shapes.rect(panelX, panelY, MAP_PANEL_W, MAP_PANEL_H);
        if (bossRoom != null) {
            // boss soba: svetel rob (bel, če je igralec v njej)
            if (bossRoom == currentRoom) shapes.setColor(1f, 1f, 1f, 1f);
            else shapes.setColor(0.95f, 0.75f, 1f, 1f);
            shapes.rect(bossX, bossY, roomSize, roomSize);
        }
        shapes.end();

        if (bossRoom != null) {
            // črka se prilagodi velikosti sobe na minimapi
            hudFont.getData().setScale(1f);
            hudFont.setColor(1f, 1f, 1f, 1f);
            hudLayout.setText(hudFont, String.valueOf(Room.BOSS_LABEL));
            float letterScale = Math.min(1.4f, roomSize * 0.7f / hudLayout.height);
            hudFont.getData().setScale(letterScale);
            hudLayout.setText(hudFont, String.valueOf(Room.BOSS_LABEL));

            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            hudFont.draw(batch, hudLayout,
                bossX + (roomSize - hudLayout.width) / 2f,
                bossY + (roomSize + hudLayout.height) / 2f);
            batch.end();
            hudFont.getData().setScale(1f);
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        roomFont.dispose();
        hudFont.dispose();
        bossHealthBar.dispose();
        player.dispose();
        enemyFactory.dispose();
        projectileFactory.dispose();
        floatingTextRenderSystem.dispose();
    }
}

package io.github.roguelike.weapon;

import io.github.roguelike.ecs.status.StatusEffectType;

/**
 * Orožja, med katerimi igralec preklaplja: tipke 1-3 = puške, tipke 4-7 = elementalni staffi
 * (indeks v loadoutu = tipka - 1). Vse teksture so 48x48 in so narisane obrnjene v desno.
 */
public enum WeaponType {
    // --- puške (1-3) ---
    PISTOL("Pistol", "Weapons/Guns/Pistol.png",
        0.25f, 1.0f, 1, 0f, 0f,
        15f, 22f, 24f,
        StatusEffectType.NONE, 0f, 0f,
        0xFF8C00FF, 12),
    RIFLE("Rifle", "Weapons/Guns/Rifle.png",
        0.10f, 0.5f, 1, 0f, 3f,
        14f, 22f, 34f,
        StatusEffectType.NONE, 0f, 0f,
        0xFF8C00FF, 12),
    SHOTGUN("Shotgun", "Weapons/Guns/Shotgun.png",
        0.75f, 0.6f, 5, 30f, 2f,
        10f, 24f, 36f,
        StatusEffectType.NONE, 0f, 0f,
        0xFF8C00FF, 12),

    // --- staffi (4-7) ---
    /** Slow: enemy se 2 s premika s polovično hitrostjo. */
    WATER_STAFF("Water Staff", "Weapons/Staffs/WaterStaff.png",
        0.45f, 0.8f, 1, 0f, 0f,
        23f, 23f, 17f,
        StatusEffectType.SLOW, 2f, 0.5f,
        0x3399FFFF, 14),
    /** Burn: 3 s damage skozi čas (60 % damage-a projectila na sekundo). */
    FIRE_STAFF("Fire Staff", "Weapons/Staffs/FireStaff.png",
        0.5f, 0.7f, 1, 0f, 0f,
        23f, 23f, 17f,
        StatusEffectType.BURN, 3f, 0.6f,
        0xFF6A0DFF, 14),
    /** Stun: enemy 1 s ne more ne hoditi ne napadati. */
    EARTH_STAFF("Earth Staff", "Weapons/Staffs/EarthStaff.png",
        0.8f, 1.0f, 1, 0f, 0f,
        23f, 23f, 17f,
        StatusEffectType.STUN, 1f, 0f,
        0x8C5A1EFF, 16),
    /** Knockback: enemy je odrinjen v smeri izstrelka (350 px/s, hitro pojema). */
    WIND_STAFF("Wind Staff", "Weapons/Staffs/WindStaff.png",
        0.35f, 0.5f, 1, 0f, 0f,
        23f, 23f, 17f,
        StatusEffectType.KNOCKBACK, 0f, 350f,
        0xB4FF66FF, 14);

    public final String displayName;
    public final String texturePath;
    /** Minimalni čas med dvema streloma (sekunde). */
    public final float fireInterval;
    /** Množitelj igralčevega damage-a (na posamezen izstrelek). */
    public final float damageMultiplier;
    /** Število izstrelkov na strel. */
    public final int pellets;
    /** Skupni kot (v stopinjah), čez katerega se razporedijo izstrelki. */
    public final float spreadDeg;
    /** Naključni odmik vsakega izstrelka (+/- stopinje). */
    public final float jitterDeg;
    /** Pivot za vrtenje orožja v pikslih teksture (roka). */
    public final float gripX, gripY;
    /** Razdalja od pivota do konice cevi / glave staffa (piksli). */
    public final float muzzleLength;
    /** Učinek na enemyja ob zadetku in njegovi parametri (glej StatusEffectType). */
    public final StatusEffectType effect;
    public final float effectDuration;
    public final float effectPower;
    /** Barva (RGBA8888) in premer izstrelka. */
    public final int projectileColor;
    public final int projectileSize;

    WeaponType(String displayName, String texturePath,
               float fireInterval, float damageMultiplier, int pellets, float spreadDeg, float jitterDeg,
               float gripX, float gripY, float muzzleLength,
               StatusEffectType effect, float effectDuration, float effectPower,
               int projectileColor, int projectileSize) {
        this.displayName = displayName;
        this.texturePath = texturePath;
        this.fireInterval = fireInterval;
        this.damageMultiplier = damageMultiplier;
        this.pellets = pellets;
        this.spreadDeg = spreadDeg;
        this.jitterDeg = jitterDeg;
        this.gripX = gripX;
        this.gripY = gripY;
        this.muzzleLength = muzzleLength;
        this.effect = effect;
        this.effectDuration = effectDuration;
        this.effectPower = effectPower;
        this.projectileColor = projectileColor;
        this.projectileSize = projectileSize;
    }
}

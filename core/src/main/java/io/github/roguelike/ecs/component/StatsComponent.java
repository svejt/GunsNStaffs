package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;

public class StatsComponent implements Component {
    public float maxHp;
    public float currentHp;
    public float damage;
    public float attackRate; // attacks per second
    public float movementSpeed;
    public boolean isBoss = false;

    public StatsComponent() {
        // Default values - will be set later
        this.maxHp = 100f;
        this.currentHp = 100f;
        this.damage = 10f;
        this.attackRate = 1f;
        this.movementSpeed = 100f;
    }

    public StatsComponent(float maxHp, float damage, float attackRate, float movementSpeed) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.damage = damage;
        this.attackRate = attackRate;
        this.movementSpeed = movementSpeed;
    }
}

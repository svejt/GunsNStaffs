package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.ComponentMapper;

public final class Mappers {
    public static final ComponentMapper<TransformComponent> TRANSFORM = ComponentMapper.getFor(TransformComponent.class);
    public static final ComponentMapper<VelocityComponent> VELOCITY = ComponentMapper.getFor(VelocityComponent.class);
    public static final ComponentMapper<FacingComponent> FACING = ComponentMapper.getFor(FacingComponent.class);
    public static final ComponentMapper<TargetComponent> TARGET = ComponentMapper.getFor(TargetComponent.class);
    public static final ComponentMapper<EnemyComponent> ENEMY = ComponentMapper.getFor(EnemyComponent.class);
    public static final ComponentMapper<EnemyStateComponent> ENEMY_STATE = ComponentMapper.getFor(EnemyStateComponent.class);
    public static final ComponentMapper<AttackComponent> ATTACK = ComponentMapper.getFor(AttackComponent.class);
    public static final ComponentMapper<AnimationComponent> ANIMATION = ComponentMapper.getFor(AnimationComponent.class);

    private Mappers() {}
}

package io.github.roguelike.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import io.github.roguelike.ecs.enemy.EnemyState;

public class EnemyStateComponent implements Component {
    public DefaultStateMachine<Entity, EnemyState> fsm;
    // čas v trenutnem stanju (ponastavi se ob vsakem enter())
    public float stateTime = 0f;
}

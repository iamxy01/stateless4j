package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.delegates.Guard;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.Transition;

public abstract class TriggerBehaviour<S, T, C> {

    private final T trigger;

    /**
     * Note that this guard gets called quite often, and sometimes multiple times per fire() call.
     * Thus, it should not be anything performance intensive.
     */
    private final Guard<S, T, C> guard;

    protected TriggerBehaviour(T trigger, Guard<S, T, C> guard) {
        this.trigger = trigger;
        this.guard = guard;
    }

    public T getTrigger() {
        return trigger;
    }

    public abstract void performAction(Transition<S, T, C> transition);

    public boolean isInternal() {
        return false;
    }

    public boolean isMatch(SelectorCondition<S, T, C> condition) {
        return guard.call(condition);
    }

    public abstract S transitionsTo(SelectorCondition<S, T, C> selectorCondition);
}

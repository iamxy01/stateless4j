package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Guard;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.Transition;

public class InternalTriggerBehaviour<S, T, C> extends TriggerBehaviour<S, T, C> {
    private final Action<S, T, C> action;

    public InternalTriggerBehaviour(T trigger, Guard<S, T, C> guard, Action<S, T, C> action) {
        super(trigger, guard);
        this.action = action;
    }

    @Override
    public void performAction(Transition<S, T, C> transition) {
        action.doIt(transition);
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public S transitionsTo(SelectorCondition<S, T, C> selectorCondition) {
        return selectorCondition.getFrom();
    }
}

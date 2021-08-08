package com.github.oxo42.stateless4j.transitions;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Guard;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

public class TransitioningTriggerBehaviour<S, T, C> extends TriggerBehaviour<S, T, C> {

    private final S destination;
    private final Action<S, T, C> action;

    public TransitioningTriggerBehaviour(T trigger, S destination, Guard<S, T, C> guard, Action<S, T, C> action) {
        super(trigger, guard);
        this.destination = destination;
        this.action = action;
    }

    @Override
    public void performAction(Transition<S, T, C> transition) {
        action.doIt(transition);
    }

    @Override
    public S transitionsTo(SelectorCondition<S, T, C> selectorCondition) {
        return destination;
    }
}

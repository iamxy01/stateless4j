package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Guard;
import com.github.oxo42.stateless4j.delegates.Selector;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.Transition;

public class DynamicTriggerBehaviour<S, T, C> extends TriggerBehaviour<S, T, C> {

    private final Selector<S, T, C> destination;
    private final Action<S, T, C> action;

    public DynamicTriggerBehaviour(T trigger, Selector<S, T, C> destination, Guard<S, T, C> guard,
                                   Action<S, T, C> action) {
        super(trigger, guard);
        assert destination != null : "destination is null";
        this.destination = destination;
        this.action = action;
    }

    @Override
    public void performAction(Transition<S, T, C> transition) {
        action.doIt(transition);
    }

    @Override
    public S transitionsTo(SelectorCondition<S, T, C> selectorCondition) {
        return destination.call(selectorCondition);
    }
}

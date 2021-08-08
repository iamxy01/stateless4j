package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.transitions.SelectorCondition;

@FunctionalInterface
public interface Guard<S, T, C> {
    boolean call(SelectorCondition<S, T, C> condition);
}

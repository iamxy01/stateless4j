package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.transitions.SelectorCondition;

/**
 * Represents a function that accepts an input and produces a result
 *
 * @param <T1> Input argument type
 * @param <R>  Result type
 */
@FunctionalInterface
public interface Selector<S, T, C> {

    /**
     * Applies this function to the given input
     *
     * @param arg1 Input argument
     * @return Result
     */
    S call(SelectorCondition<S, T, C> condition);
}

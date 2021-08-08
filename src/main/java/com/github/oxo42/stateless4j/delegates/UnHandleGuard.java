package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.transitions.SelectorCondition;

/**
 * Represents an operation that accepts an input and returns no result
 *
 * @param <T>  The type of the input to the operation
 * @param <T1> The type of the input to the operation
 * @param <T2> The type of the input to the operation
 */
@FunctionalInterface
public interface UnHandleGuard<S, T, C> {

    /**
     * Performs this operation on the given input
     *
     * @param condition Input argument
     */
    void doIt(SelectorCondition<S, T, C> condition);
}

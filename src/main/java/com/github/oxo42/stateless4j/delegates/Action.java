package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.transitions.Transition;

/**
 * Represents an operation that accepts an input and returns no result
 *
 * @param <T> The type of the input to the operation
 */
@FunctionalInterface
public interface Action<S, T, C> {

    /**
     * Performs this operation on the given input
     *
     * @param transition Input argument
     */
    void doIt(Transition<S, T, C> transition);
}

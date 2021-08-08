package com.github.oxo42.stateless4j.delegates;

import com.github.oxo42.stateless4j.StateRepresentation;

/**
 * Represents a function that accepts an input and produces a result
 *
 * @param <T1> Input argument type
 * @param <R>  Result type
 */
@FunctionalInterface
public interface StateRepresentationSelector<S, T, C> {

    /**
     * Applies this function to the given input
     *
     * @param superState Input argument
     * @return Result
     */
    StateRepresentation<S, T, C> call(S superState);
}

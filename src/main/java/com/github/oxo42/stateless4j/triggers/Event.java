package com.github.oxo42.stateless4j.triggers;

import com.github.oxo42.stateless4j.conversion.ParameterConversion;

/**
 * @author yuchaoyang
 */
public interface Event<T, C> {

    T getTrigger();

    Class<C> getContextClass();

    /**
     * Ensure that the supplied arguments are compatible with those configured for this trigger
     *
     * @param context Args
     */
    default void validateParameters(C context) {
        assert context != null : "context is null";
        ParameterConversion.validate(context, getContextClass());
    }

    /*@Override
    default String toString() {
        return toString(getTrigger(), getContextClass());
    }

    static <TTrigger> String toString(TTrigger trigger, Object context) {
        if (context == null) {
            return trigger.toString();
        } else {
            StringBuilder b = new StringBuilder(trigger.toString());
            b.append('(');

            if (context instanceof Class) {
                b.append(((Class<?>) context).getSimpleName());
            } else {
                b.append(context);
            }

            b.append(')');
            return b.toString();
        }
    }*/
}

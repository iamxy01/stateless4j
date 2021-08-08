package com.github.oxo42.stateless4j.conversion;

public final class ParameterConversion {

    private ParameterConversion() {
    }

    public static void validate(Object arg, Class<?> expected) {
        if (arg != null && !expected.isAssignableFrom(arg.getClass())) {
            throw new IllegalStateException(
                    String.format("The argument is of type %s but must be of type %s.", arg.getClass(), expected));
        }
    }
}

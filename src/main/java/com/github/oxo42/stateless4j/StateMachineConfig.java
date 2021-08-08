package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.StateRepresentationSelector;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.Event;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The state machine configuration. Reusable.
 */
public class StateMachineConfig<TState, TTrigger, Context> {

    private final Map<TState, StateRepresentation<TState, TTrigger, Context>> stateConfiguration = new HashMap<>();
    private final Map<TTrigger, Event<TTrigger, Context>> triggerConfiguration = new HashMap<>();

    /**
     * Return StateRepresentation for the specified state. May return null.
     *
     * @param state The state
     * @return StateRepresentation for the specified state, or null.
     */
    public StateRepresentation<TState, TTrigger, Context> getRepresentation(TState state) {
        return stateConfiguration.get(state);
    }

    /**
     * Return StateRepresentation for the specified state. Creates representation if it does not exist.
     *
     * @param state The state
     * @return StateRepresentation for the specified state.
     */
    private StateRepresentation<TState, TTrigger, Context> getOrCreateRepresentation(TState state) {
        StateRepresentation<TState, TTrigger, Context> result = stateConfiguration.get(state);
        if (result == null) {
            result = new StateRepresentation<>(state);
            stateConfiguration.put(state, result);
        }

        return result;
    }

    public Event<TTrigger, Context> getTriggerConfiguration(TTrigger trigger) {
        return triggerConfiguration.get(trigger);
    }

    public boolean isTriggerConfigured(TTrigger trigger) {
        return triggerConfiguration.containsKey(trigger);
    }

    /**
     * Begin configuration of the entry/exit actions and allowed transitions
     * when the state machine is in a particular state
     *
     * @param state The state to configure
     * @return A configuration object through which the state can be configured
     */
    public StateConfiguration<TState, TTrigger, Context> configure(TState state) {
        return new StateConfiguration<>(getOrCreateRepresentation(state), new StateRepresentationSelector<TState, TTrigger, Context>() {

            @Override
            public StateRepresentation<TState, TTrigger, Context> call(TState arg0) {
                return getOrCreateRepresentation(arg0);
            }
        });
    }

    private void saveTriggerConfiguration(Event<TTrigger, Context> event) {
        if (triggerConfiguration.containsKey(event.getTrigger())) {
            throw new IllegalStateException("Parameters for the trigger '" + event + "' have already been configured.");
        }

        triggerConfiguration.put(event.getTrigger(), event);
    }

    /**
     * Specify the arguments that must be supplied when a specific trigger is fired
     *
     * @param trigger      The underlying trigger value
     * @param contextClass Class argument
     * @return An object that can be passed to the fire() method in order to fire the parameterised trigger
     */
    public Event<TTrigger, Context> setTriggerParameters(TTrigger trigger, Class<Context> contextClass) {
        Event<TTrigger, Context> configuration = new Event<TTrigger, Context>() {

            @Override
            public TTrigger getTrigger() {
                return trigger;
            }

            @Override
            public Class<Context> getContextClass() {
                return contextClass;
            }
        };

        saveTriggerConfiguration(configuration);
        return configuration;
    }

    public void generateDotFileInto(final OutputStream dotFile) throws IOException {
        generateDotFileInto(dotFile, false);
    }

    public void generateDotFileInto(final OutputStream dotFile, boolean printLabels) throws IOException {
        try (OutputStreamWriter w = new OutputStreamWriter(dotFile, StandardCharsets.UTF_8)) {
            PrintWriter writer = new PrintWriter(w);
            writer.write("digraph G {\n");
            for (Map.Entry<TState, StateRepresentation<TState, TTrigger, Context>> entry : this.stateConfiguration.entrySet()) {
                Map<TTrigger, List<TriggerBehaviour<TState, TTrigger, Context>>> behaviours = entry.getValue().getTriggerBehaviours();
                for (Map.Entry<TTrigger, List<TriggerBehaviour<TState, TTrigger, Context>>> behaviour : behaviours.entrySet()) {
                    for (TriggerBehaviour<TState, TTrigger, Context> triggerBehaviour : behaviour.getValue()) {
                        if (triggerBehaviour instanceof TransitioningTriggerBehaviour) {
                            SelectorCondition<TState, TTrigger, Context> selectorCondition = new SelectorCondition<>(null, triggerBehaviour.getTrigger(), null);//todo 和原来比较下是否逻辑正确
                            TState destination = triggerBehaviour.transitionsTo(selectorCondition);
                            if (printLabels) {
                                writer.write(String.format("\t%s -> %s [label = \"%s\" ];\n", entry.getKey(), destination, triggerBehaviour.getTrigger()));
                            } else {
                                writer.write(String.format("\t%s -> %s;\n", entry.getKey(), destination));
                            }
                        }
                    }
                }
            }
            writer.write("}");
        }
    }

}

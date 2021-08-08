package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Trace;
import com.github.oxo42.stateless4j.delegates.UnHandleGuard;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Models behaviour as transitions between a finite set of states
 *
 * @param <S> The type used to represent the states
 * @param <T> The type used to represent the triggers that cause state transitions
 */
public class StateMachine<S, T, C> {

    private static final String TRIGGER_IS_NULL = "trigger is null";
    protected final StateMachineConfig<S, T, C> config;
    protected final Supplier<S> stateAccessor;
    protected final Consumer<S> stateMutator;
    private Trace<S, T> trace = null;
    private boolean isStarted = false;
    private S initialState;
    protected UnHandleGuard<S, T, C> unhandledTriggerAction = guardCondition -> {
        throw new IllegalStateException(
                String.format(
                        "No valid leaving transitions are permitted from state '%s' for trigger '%s'. Consider ignoring the trigger.",
                        guardCondition.getFrom(), guardCondition.getTrigger())
        );
    };
    private C context;

    /**
     * Construct a state machine
     *
     * @param initialState The initial state
     */
    public StateMachine(S initialState) {
        this(initialState, new StateMachineConfig<>());
    }

    /**
     * Construct a state machine
     *
     * @param initialState The initial state
     * @param config       State machine configuration
     */
    public StateMachine(S initialState, StateMachineConfig<S, T, C> config) {
        this.initialState = initialState;
        this.config = config;
        final StateReference<S> reference = new StateReference<>();
        reference.setState(initialState);
        stateAccessor = reference::getState;
        stateMutator = reference::setState;
    }

    /**
     * Construct a state machine with external state storage.
     *
     * @param initialState  The initial state
     * @param stateAccessor State accessor
     * @param stateMutator  State mutator
     */
    public StateMachine(S initialState, Supplier<S> stateAccessor, Consumer<S> stateMutator, StateMachineConfig<S, T, C> config) {
        this.config = config;
        this.stateAccessor = stateAccessor;
        this.stateMutator = stateMutator;
        stateMutator.accept(initialState);
    }

    /**
     * Fire initial transition into the initial state.
     * All super-states are entered too.
     * <p>
     * This method can be called only once, before state machine is used.
     */
    public void fireInitialTransition() {
        S currentState = getCurrentRepresentation().getUnderlyingState();
        if (isStarted || !currentState.equals(initialState)) {
            throw new IllegalStateException("Firing initial transition after state machine has been started");
        }
        isStarted = true;
        Transition<S, T, C> initialTransition = new Transition<>(null, currentState, null, context);
        getCurrentRepresentation().enter(initialTransition);
    }

    public StateConfiguration<S, T, C> configure(S state) {
        return config.configure(state);
    }

    public StateMachineConfig<S, T, C> configuration() {
        return config;
    }

    /**
     * The current state
     *
     * @return The current state
     */
    public S getState() {
        return stateAccessor.get();
    }

    private void setState(S value) {
        stateMutator.accept(value);
    }

    /**
     * The currently-permissible trigger values
     *
     * @return The currently-permissible trigger values
     */
    public List<T> getPermittedTriggers() {
        return getCurrentRepresentation().getPermittedTriggers();
    }

    StateRepresentation<S, T, C> getCurrentRepresentation() {
        StateRepresentation<S, T, C> representation = config.getRepresentation(getState());
        return representation == null ? new StateRepresentation<>(getState()) : representation;
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked
     *
     * @param trigger The trigger to fire
     */
    public void fire(T trigger) {
        publicFire(trigger, context);
    }

    /**
     * Transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state and entering the new one
     * will be invoked.
     *
     * @param trigger The trigger to fire
     * @param context context
     */
    public void fire(T trigger, C context) {
        assert trigger != null : TRIGGER_IS_NULL;
        this.context = context;
        publicFire(trigger, context);
    }

    protected void publicFire(T trigger, C context) {
        isStarted = true;
        if (trace != null) {
            trace.trigger(trigger);
        }

        SelectorCondition<S, T, C> selectorCondition = new SelectorCondition<>(getState(), trigger, context);
        TriggerBehaviour<S, T, C> triggerBehaviour = getCurrentRepresentation().tryFindHandler(trigger, selectorCondition);
        if (triggerBehaviour == null) {
            SelectorCondition<S, T, C> condition = new SelectorCondition<>(getCurrentRepresentation().getUnderlyingState(), trigger, context);
            unhandledTriggerAction.doIt(condition);
            return;
        }

        S source = getState();
        if (triggerBehaviour.isInternal()) {
            S destination = triggerBehaviour.transitionsTo(selectorCondition);
            Transition<S, T, C> transition = new Transition<>(source, destination, trigger, context);
            triggerBehaviour.performAction(transition);
        } else {

            S destination = triggerBehaviour.transitionsTo(selectorCondition);
            Transition<S, T, C> transition = new Transition<>(source, destination, trigger, context);

            getCurrentRepresentation().exit(transition);
            triggerBehaviour.performAction(transition);
            setState(destination);
            getCurrentRepresentation().enter(transition);
            if (trace != null) {
                trace.transition(trigger, source, destination);
            }
        }
    }

    /**
     * Override the default behaviour of throwing an exception when an unhandled trigger is fired
     *
     * @param unhandledTriggerAction An action to call with state, trigger and params when an unhandled trigger is fired
     */
    public void onUnhandledTrigger(UnHandleGuard<S, T, C> unhandledTriggerAction) {
        if (unhandledTriggerAction == null) {
            throw new IllegalStateException("unhandledTriggerAction");
        }
        this.unhandledTriggerAction = unhandledTriggerAction;
    }

    /**
     * Determine if the state machine is in the supplied state
     *
     * @param state The state to test for
     * @return True if the current state is equal to, or a substate of, the supplied state
     */
    public boolean isInState(S state) {
        return getCurrentRepresentation().isIncludedIn(state);
    }

    /**
     * Returns true if {@code trigger} can be fired  in the current state
     *
     * @param trigger Trigger to test
     * @return True if the trigger can be fired, false otherwise
     */
    public boolean canFire(T trigger) {
        return getCurrentRepresentation().canHandle(trigger, null);
    }

    public boolean canFire(T trigger, SelectorCondition<S, T, C> condition) {
        return getCurrentRepresentation().canHandle(trigger, condition);
    }

    /**
     * Set tracer delegate. Set trace delegate to investigate what the state machine is doing
     * at runtime. Trace delegate will be called on {@link #fire(Object)} and on transition.
     *
     * @param trace Trace delegate or null, if trace should be disabled
     */
    public void setTrace(Trace<S, T> trace) {
        this.trace = trace;
    }

    /**
     * A human-readable representation of the state machine
     *
     * @return A description of the current state and permitted triggers
     */
    @Override
    public String toString() {
        List<T> permittedTriggers = getPermittedTriggers();
        List<String> parameters = new ArrayList<>();

        for (T tTrigger : permittedTriggers) {
            parameters.add(tTrigger.toString());
        }

        StringBuilder params = new StringBuilder();
        String delim = "";
        for (String param : parameters) {
            params.append(delim);
            params.append(param);
            delim = ", ";
        }

        return String.format(
                "StateMachine {{ State = %s, PermittedTriggers = {{ %s }}}}",
                getState(),
                params.toString());
    }

    public C getContext() {
        return context;
    }
}

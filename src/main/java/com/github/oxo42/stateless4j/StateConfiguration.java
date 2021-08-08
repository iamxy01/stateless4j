package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.delegates.Guard;
import com.github.oxo42.stateless4j.delegates.Selector;
import com.github.oxo42.stateless4j.delegates.StateRepresentationSelector;
import com.github.oxo42.stateless4j.transitions.TransitioningTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.DynamicTriggerBehaviour;
import com.github.oxo42.stateless4j.triggers.InternalTriggerBehaviour;

public class StateConfiguration<S, T, C> {
    private static final String GUARD_IS_NULL = "guard is null";
    private static final String ENTRY_ACTION_IS_NULL = "entryAction is null";
    private static final String EXIT_ACTION_IS_NULL = "exitAction is null";
    private static final String ACTION_IS_NULL = "action is null";
    private static final String TRIGGER_IS_NULL = "trigger is null";
    private static final String DESTINATION_STATE_SELECTOR_IS_NULL = "destinationStateSelector is null";

    private final Guard<S, T, C> NO_GUARD = condition -> true;

    private final Action<S, T, C> NO_ACTION = transition -> {
    };
    private final StateRepresentation<S, T, C> representation;
    private final StateRepresentationSelector<S, T, C> lookup;

    public StateConfiguration(final StateRepresentation<S, T, C> representation, final StateRepresentationSelector<S, T, C> lookup) {
        assert representation != null : "representation is null";
        assert lookup != null : "lookup is null";
        this.representation = representation;
        this.lookup = lookup;
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permit(T trigger, S toState) {
        enforceNotIdentityTransition(toState);
        return publicPermit(trigger, toState);
    }

    /*public StateConfiguration<S, T, C> permit(TriggerWithContext<T, C> trigger, S toState) {
        enforceNotIdentityTransition(toState);
        return publicPermitIf(trigger, toState, NO_GUARD, NO_ACTION);
    }

    public StateConfiguration<S, T, C> permit(TriggerWithContext<T, C> trigger, S toState, final TransitionAction<S, T, C> action) {
        enforceNotIdentityTransition(toState);
        return publicPermit(trigger, toState, action);
    }*/

    public StateConfiguration<S, T, C> permit(T trigger, S toState, final Action<S, T, C> action) {
        enforceNotIdentityTransition(toState);
        return publicPermit(trigger, toState, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state.
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger          The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @param action           The action to be performed "during" transition
     * @return The receiver
     */
    /*public StateConfiguration<S, T,C> permit(T trigger, S toState, final TransitionAction action) {
        enforceNotIdentityTransition(toState);
        return publicPermit(trigger, toState, action);
    }*/

    /**
     * Accept the specified trigger and transition to the destination state if selector is true
     *
     * @param trigger The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitIf(T trigger, S toState, Guard<S, T, C> guard) {
        enforceNotIdentityTransition(toState);
        return publicPermitIf(trigger, toState, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state if selector is true
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @param action  The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitIf(T trigger, S toState, Guard<S, T, C> guard, Action<S, T, C> action) {
        enforceNotIdentityTransition(toState);
        return publicPermitIf(trigger, toState, guard, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state if selector true, otherwise ignore
     *
     * @param trigger The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitIfElseIgnore(T trigger, S toState, final Guard<S, T, C> guard) {
        enforceNotIdentityTransition(toState);
        ignoreIf(trigger, condition -> !guard.call(condition));
        return publicPermitIf(trigger, toState, guard);
    }

    /**
     * Accept the specified trigger and transition to the destination state if selector true, otherwise ignore
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of
     * the destination state.
     *
     * @param trigger The accepted trigger
     * @param toState The state that the trigger will cause a transition to
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @param action  The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitIfElseIgnore(T trigger, S toState, final Guard<S, T, C> guard, Action<S, T, C> action) {
        enforceNotIdentityTransition(toState);
        ignoreIf(trigger, condition -> !guard.call(condition));
        return publicPermitIf(trigger, toState, guard, action);
    }

    /**
     * Accept the specified trigger, execute action and stay in state
     * <p>
     * Applies to the current state only. No exit or entry actions will be
     * executed and the state will not change. The only thing that happens is
     * the execution of a given action.
     *
     * @param trigger The accepted trigger
     * @param action  The action to be performed
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitInternal(T trigger, Action<S, T, C> action) {
        return permitInternalIf(trigger, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger, execute action and stay in state
     * <p>
     * Applies to the current state only. No exit or entry actions will be
     * executed and the state will not change. The only thing that happens is
     * the execution of a given action.
     * <p>
     * The action is only executed if the given selector returns true. Otherwise
     * this transition will not be taken into account (so it does not count
     * as 'ignore', then).
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @param action  The action to be performed
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitInternalIf(T trigger, Guard<S, T, C> guard, Action<S, T, C> action) {
        assert guard != null : GUARD_IS_NULL;
        assert action != null : ACTION_IS_NULL;
        representation.addTriggerBehaviour(new InternalTriggerBehaviour<>(trigger, guard, action));
        return this;
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitReentry(T trigger) {
        return publicPermit(trigger, representation.getUnderlyingState());
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger The accepted trigger
     * @param action  The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitReentry(T trigger, Action<S, T, C> action) {
        return publicPermit(trigger, representation.getUnderlyingState(), action);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitReentryIf(T trigger, Guard<S, T, C> guard) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitReentryIf(T trigger, Guard<S, T, C> guard, Action<S, T, C> action) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard, action);
    }

    /**
     * ignore the specified trigger when in the configured state
     *
     * @param trigger The trigger to ignore
     * @return The receiver
     */
    public StateConfiguration<S, T, C> ignore(T trigger) {
        return ignoreIf(trigger, NO_GUARD);
    }

    /**
     * ignore the specified trigger when in the configured state, if the selector returns true
     *
     * @param trigger The trigger to ignore
     * @param guard   Function that must return true in order for the trigger to be ignored
     * @return The receiver
     */
    public StateConfiguration<S, T, C> ignoreIf(T trigger, Guard<S, T, C> guard) {
        assert guard != null : GUARD_IS_NULL;
        representation.addTriggerBehaviour(new InternalTriggerBehaviour<>(trigger, guard, NO_ACTION));
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> onEntry(final Action<S, T, C> entryAction) {
        assert entryAction != null : ENTRY_ACTION_IS_NULL;
        representation.addEntryAction(entryAction);
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> onEntryFrom(T trigger, final Action<S, T, C> entryAction) {
        assert entryAction != null : ENTRY_ACTION_IS_NULL;
        representation.addEntryAction(trigger, entryAction);
        return this;
    }

    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration<S, T, C> onExit(Action<S, T, C> exitAction) { //todo 确认下from和to是否对的
        assert exitAction != null : EXIT_ACTION_IS_NULL;
        representation.addExitAction(exitAction);
        return this;
    }

    /**
     * Sets the superstate that the configured state is a substate of
     * <p>
     * Substates inherit the allowed transitions of their superstate.
     * When entering directly into a substate from outside of the superstate,
     * entry actions for the superstate are executed.
     * Likewise when leaving from the substate to outside the supserstate,
     * exit actions for the superstate will execute.
     *
     * @param superState The superState
     * @return The receiver
     */
    public StateConfiguration<S, T, C> subStateOf(S superState) {
        StateRepresentation<S, T, C> superRepresentation = lookup.call(superState);
        representation.setSuperState(superRepresentation);
        superRepresentation.addSubstate(representation);
        return this;
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger  The accepted trigger
     * @param selector Function to calculate the state that the trigger will cause a transition to
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitDynamic(T trigger, final Selector<S, T, C> selector) {
        return permitDynamicIf(trigger, selector, NO_GUARD);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action and before the onEntry action (of the re-entered state).
     *
     * @param trigger  The accepted trigger
     * @param selector Function to calculate the state that the trigger will cause a transition to
     * @param action   The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitDynamic(T trigger, final Selector<S, T, C> selector, Action<S, T, C> action) {
        return permitDynamicIf(trigger, selector, NO_GUARD, action);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     *
     * @param trigger  The accepted trigger
     * @param selector Function to calculate the state that the trigger will cause a transition to
     * @param guard    Function that must return true in order for the  trigger to be accepted
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitDynamicIf(T trigger,
                                                       final Selector<S, T, C> selector,
                                                       Guard<S, T, C> guard) {
        assert trigger != null : TRIGGER_IS_NULL;
        assert selector != null : DESTINATION_STATE_SELECTOR_IS_NULL;
        return publicPermitDynamicIf(trigger, selector, guard, NO_ACTION);
    }

    /**
     * Accept the specified trigger and transition to the destination state, calculated dynamically by the supplied
     * function
     * <p>
     * Additionally a given action is performed when transitioning. This action will be called after
     * the onExit action of the current state and before the onEntry action of the destination state.
     *
     * @param trigger  The accepted trigger
     * @param selector Function to calculate the state that the trigger will cause a transition to
     * @param guard    Function that must return true in order for the  trigger to be accepted
     * @param action   The action to be performed "during" transition
     * @return The receiver
     */
    public StateConfiguration<S, T, C> permitDynamicIf(T trigger,
                                                       final Selector<S, T, C> selector,
                                                       Guard<S, T, C> guard,
                                                       final Action<S, T, C> action) {
        assert trigger != null : TRIGGER_IS_NULL;
        assert selector != null : DESTINATION_STATE_SELECTOR_IS_NULL;
        return publicPermitDynamicIf(trigger, selector, guard, action);
    }

    void enforceNotIdentityTransition(S destination) {
        if (destination.equals(representation.getUnderlyingState())) {
            throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. To accept a trigger without changing state, use either ignore(), permitInternal() or permitReentry().");
        }
    }

    StateConfiguration<S, T, C> publicPermit(T trigger, S toState) {
        return publicPermitIf(trigger, toState, NO_GUARD, NO_ACTION);
    }

    StateConfiguration<S, T, C> publicPermit(T trigger, S toState, Action<S, T, C> action) {
        return publicPermitIf(trigger, toState, NO_GUARD, action);
    }

    StateConfiguration<S, T, C> publicPermitIf(T trigger, S toState, Guard<S, T, C> guard) {
        return publicPermitIf(trigger, toState, guard, NO_ACTION);
    }

    StateConfiguration<S, T, C> publicPermitIf(T trigger, S toState, Guard<S, T, C> guard, Action<S, T, C> action) {
        assert action != null : ACTION_IS_NULL;
        assert guard != null : GUARD_IS_NULL;
        representation.addTriggerBehaviour(new TransitioningTriggerBehaviour<>(trigger, toState, guard, action));
        return this;
    }

    StateConfiguration<S, T, C> publicPermitDynamic(T trigger, Selector<S, T, C> selector) {
        return publicPermitDynamicIf(trigger, selector, NO_GUARD, NO_ACTION);
    }

    StateConfiguration<S, T, C> publicPermitDynamicIf(T trigger, Selector<S, T, C> selector, Guard<S, T, C> guard) {
        assert selector != null : DESTINATION_STATE_SELECTOR_IS_NULL;
        assert guard != null : GUARD_IS_NULL;
        representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, selector, guard, NO_ACTION));
        return this;
    }

    StateConfiguration<S, T, C> publicPermitDynamicIf(T trigger, Selector<S, T, C> selector, Guard<S, T, C> guard, Action<S, T, C> action) {
        assert selector != null : DESTINATION_STATE_SELECTOR_IS_NULL;
        assert guard != null : GUARD_IS_NULL;
        representation.addTriggerBehaviour(new DynamicTriggerBehaviour<>(trigger, selector, guard, action));
        return this;
    }
}

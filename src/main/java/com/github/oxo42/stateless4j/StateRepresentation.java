package com.github.oxo42.stateless4j;

import com.github.oxo42.stateless4j.delegates.Action;
import com.github.oxo42.stateless4j.transitions.SelectorCondition;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.triggers.TriggerBehaviour;

import java.util.*;

/**
 * @author yuchaoyang
 */
public class StateRepresentation<S, T, C> {

    private static final String ACTION_IS_NULL = "action is null";
    private static final String TRANSITION_IS_NULL = "transition is null";
    private final S state;

    private final Map<T, List<TriggerBehaviour<S, T, C>>> triggerBehaviours = new HashMap<>();
    private final List<Action<S, T, C>> entryActions = new ArrayList<>();
    private final List<Action<S, T, C>> exitActions = new ArrayList<>();
    private final List<StateRepresentation<S, T, C>> subStates = new ArrayList<>();
    private StateRepresentation<S, T, C> superState;

    public StateRepresentation(S state) {
        this.state = state;
    }

    protected Map<T, List<TriggerBehaviour<S, T, C>>> getTriggerBehaviours() {
        return triggerBehaviours;
    }

    public Boolean canHandle(T trigger, SelectorCondition<S, T, C> condition) {
        return tryFindHandler(trigger, condition) != null;
    }

    public TriggerBehaviour<S, T, C> tryFindHandler(T trigger, SelectorCondition<S, T, C> condition) {
        TriggerBehaviour<S, T, C> result = tryFindLocalHandler(trigger, condition);
        if (result == null && superState != null) {
            result = superState.tryFindHandler(trigger, condition);
        }
        return result;
    }

    TriggerBehaviour<S, T, C> tryFindLocalHandler(T trigger, SelectorCondition<S, T, C> condition) {
        List<TriggerBehaviour<S, T, C>> possible = triggerBehaviours.get(trigger);
        if (possible == null) {
            return null;
        }

        List<TriggerBehaviour<S, T, C>> actual = new ArrayList<>();
        for (TriggerBehaviour<S, T, C> triggerBehaviour : possible) {
            if (triggerBehaviour.isMatch(condition)) {
                actual.add(triggerBehaviour);
            }
        }

        if (actual.size() > 1) {
            throw new IllegalStateException("Multiple permitted exit transitions are configured from state '" + state + "' for trigger '" + trigger + "'. Guard clauses must be mutually exclusive.");
        }

        return actual.isEmpty() ? null : actual.get(0);
    }

    public void addEntryAction(final T trigger, final Action<S, T, C> action) {
        assert action != null : ACTION_IS_NULL;

        entryActions.add(transition -> {
            T transTrigger = transition.getTrigger();
            if (transTrigger != null && transTrigger.equals(trigger)) {//todo  应该有个提示？
                action.doIt(transition);
            }
        });
    }

    public void addEntryAction(Action<S, T, C> action) {
        assert action != null : ACTION_IS_NULL;
        entryActions.add(action);
    }

    public void insertEntryAction(Action<S, T, C> action) {
        assert action != null : ACTION_IS_NULL;
        entryActions.add(0, action);
    }

    public void addExitAction(Action<S, T, C> action) {
        assert action != null : ACTION_IS_NULL;
        exitActions.add(action);
    }

    public void enter(Transition<S, T, C> transition) {
        assert transition != null : TRANSITION_IS_NULL;

        if (transition.isReentry()) {
            executeEntryActions(transition);
        } else if (!includes(transition.getSource())) {
            if (superState != null) {
                superState.enter(transition);
            }

            executeEntryActions(transition);
        }
    }

    public void exit(Transition<S, T, C> transition) {
        assert transition != null : TRANSITION_IS_NULL;

        if (transition.isReentry()) {
            executeExitActions(transition);
        } else if (!includes(transition.getDestination())) {
            executeExitActions(transition);
            if (superState != null) {
                superState.exit(transition);
            }
        }
    }

    void executeEntryActions(Transition<S, T, C> transition) {
        assert transition != null : TRANSITION_IS_NULL;
        for (Action<S, T, C> action : entryActions) {
            action.doIt(transition);
        }
    }

    void executeExitActions(Transition<S, T, C> transition) {
        assert transition != null : TRANSITION_IS_NULL;
        for (Action<S, T, C> action : exitActions) {
            action.doIt(transition);
        }
    }

    public void addTriggerBehaviour(TriggerBehaviour<S, T, C> triggerBehaviour) {
        List<TriggerBehaviour<S, T, C>> allowed;
        if (!triggerBehaviours.containsKey(triggerBehaviour.getTrigger())) {
            allowed = new ArrayList<>();
            triggerBehaviours.put(triggerBehaviour.getTrigger(), allowed);
        }
        allowed = triggerBehaviours.get(triggerBehaviour.getTrigger());
        allowed.add(triggerBehaviour);
    }

    public StateRepresentation<S, T, C> getSuperState() {
        return superState;
    }

    public void setSuperState(StateRepresentation<S, T, C> value) {
        superState = value;
    }

    public S getUnderlyingState() {
        return state;
    }

    public void addSubstate(StateRepresentation<S, T, C> substate) {
        assert substate != null : "substate is null";
        subStates.add(substate);
    }

    public boolean includes(S stateToCheck) {
        for (StateRepresentation<S, T, C> s : subStates) {
            if (s.includes(stateToCheck)) {
                return true;
            }
        }
        return this.state.equals(stateToCheck);
    }

    public boolean isIncludedIn(S stateToCheck) {
        return this.state.equals(stateToCheck) || (superState != null && superState.isIncludedIn(stateToCheck));
    }

    public List<T> getPermittedTriggers() {
        Set<T> result = new HashSet<>();

        for (T t : triggerBehaviours.keySet()) {
            for (TriggerBehaviour<S, T, C> v : triggerBehaviours.get(t)) {
                SelectorCondition<S, T, C> condition = new SelectorCondition<>(null, v.getTrigger(), null);//todo
                if (v.isMatch(condition)) {
                    result.add(t);
                    break;
                }
            }
        }

        if (getSuperState() != null) {
            result.addAll(getSuperState().getPermittedTriggers());
        }

        return new ArrayList<>(result);
    }
}

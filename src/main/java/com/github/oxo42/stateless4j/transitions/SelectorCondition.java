package com.github.oxo42.stateless4j.transitions;

/**
 * @author yuchaoyang
 */
public class SelectorCondition<S, T, C> {
    private S from;
    private T trigger;
    private C context;

    public SelectorCondition(S from, T trigger, C context) {
        this.from = from;
        this.trigger = trigger;
        this.context = context;
    }

    public S getFrom() {
        return from;
    }

    public void setFrom(S from) {
        this.from = from;
    }

    public T getTrigger() {
        return trigger;
    }

    public void setTrigger(T trigger) {
        this.trigger = trigger;
    }

    public C getContext() {
        return context;
    }

    public void setContext(C context) {
        this.context = context;
    }
}

package edu.lsu.cct.javalin;

public class Var<T> {
    final Guard g;
    T data;

    Var(T t,Guard g) { data = t; this.g = g; }

    public void set(T t) {
        assert Guard.has(g);
        data = t;
    }

    public T get() {
        assert Guard.has(g);
        return data;
    }
}

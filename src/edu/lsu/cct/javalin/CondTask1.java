package edu.lsu.cct.javalin;

public abstract class CondTask1<T> extends CondTask {
    Var<T> t;
    public void set1(Var<T> t) { this.t = t; }

    public abstract boolean check(Var<T> t);

    public final void run() {
        if(!done)
            done = check(t);
    }
}

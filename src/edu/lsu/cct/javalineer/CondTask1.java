package edu.lsu.cct.javalineer;

public class CondTask1<T> extends CondTask {
    Var<T> t;
    public void set1(Var<T> t) { this.t = t; }

    public final CondCheck1<T> check;
    public CondTask1(CondCheck1<T> check) {
        this.check = check;
    }

    public final void run() {
        if(!done)
            done = check.check(t);
    }
}

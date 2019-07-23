package edu.lsu.cct.javalin;

public abstract class CondTask2<T1,T2> extends CondTask {
    Var<T1> t1;
    Var<T2> t2;
    public void set1(Var<T1> t1) { this.t1 = t1; }
    public void set2(Var<T2> t2) { this.t2 = t2; }

    public abstract boolean check(Var<T1> t1,Var<T2> t2);

    public final void run() {
        if(!done)
            done = check(t1,t2);
    }
}

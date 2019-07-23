package edu.lsu.cct.javalin;

public abstract class CondTask3<T1,T2,T3> extends CondTask {
    Var<T1> t1;
    Var<T2> t2;
    Var<T3> t3;
    public void set1(Var<T1> t1) { this.t1 = t1; }
    public void set2(Var<T2> t2) { this.t2 = t2; }
    public void set3(Var<T3> t3) { this.t3 = t3; }

    public abstract boolean check(Var<T1> t1,Var<T2> t2,Var<T3> t3);

    final public void run() {
        if(!done)
            done = check(t1,t2,t3);
    }
}

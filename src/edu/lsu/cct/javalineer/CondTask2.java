package edu.lsu.cct.javalineer;

public class CondTask2<T1,T2> extends CondTask {
    Var<T1> t1;
    Var<T2> t2;
    public void set1(Var<T1> t1) { this.t1 = t1; }
    public void set2(Var<T2> t2) { this.t2 = t2; }

    public final CondCheck2<T1,T2> check;
    public CondTask2(CondCheck2<T1,T2> check) {
        this.check = check;
    }

    public final void run() {
        if (!done) {
            try {
                done = check.check(t1, t2);
                if (done) {
                    fut.complete(null);
                }
            } catch (Exception e) {
                done = true;
                fut.completeExceptionally(e);
            }
        }
    }
}

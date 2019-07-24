package edu.lsu.cct.javalin;

public interface GuardTask2<T1,T2> {
    void run(Var<T1> v1,Var<T2> v2);
}

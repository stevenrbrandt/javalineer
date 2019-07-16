package edu.lsu.cct.javalin;

public interface CondArg1<T> {
    void run(Var<T> v,Future<Boolean> fb);
}

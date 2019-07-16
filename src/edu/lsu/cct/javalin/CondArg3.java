package edu.lsu.cct.javalin;

import java.util.List;

public interface CondArg3<T1,T2,T3> {
    void run(Var<T1> v1,Var<T2> v2,Var<T3> v3,Future<Boolean> fb);
}

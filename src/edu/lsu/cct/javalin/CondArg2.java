package edu.lsu.cct.javalin;

import java.util.List;

public interface CondArg2<T1,T2> {
    void run(Var<T1> v1,Var<T2> v2,Future<Boolean> fb);
}

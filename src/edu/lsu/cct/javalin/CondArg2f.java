package edu.lsu.cct.javalin;

import java.util.List;

public interface CondArg2f<T1,T2> {
    boolean run(Var<T1> v1,Var<T2> v2);
}

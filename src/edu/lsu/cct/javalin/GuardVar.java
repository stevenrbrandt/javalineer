package edu.lsu.cct.javalin;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

public class GuardVar<T> extends Guard {
    final Var<T> var;

    public GuardVar(T t) { var = new Var<>(t,this); }

    public void runGuarded(Consumer<Var<T>> c) {
        runGuarded(()->{ c.accept(var); });
    }

    public static void runGuarded(List<GuardVar<?>> guards,final Consumer<List<Var<?>>> vars) {
        TreeSet<Guard> ts = new TreeSet<>();
        List<Var<?>> result = new ArrayList<>();
        for(GuardVar<?> gv : guards) {
            ts.add(gv);
            result.add(gv.var);
        }
        Runnable r = () -> { vars.accept(result); };
        Guard.runGuarded(ts,r);
    }
}

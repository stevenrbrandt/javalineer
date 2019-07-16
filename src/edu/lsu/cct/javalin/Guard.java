/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalin;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.function.Consumer;

/**
 *
 * @author sbrandt
 */
public class Guard implements Comparable<Guard> {
    static AtomicInteger idSeq = new AtomicInteger(0);
    public final int id = idSeq.getAndIncrement();

    public int compareTo(Guard g) {
        return id - g.id;
    }
    
    public String toString() {
        return "Guard("+id+")";
    }

    public Guard getGuard() {
        return this;
    }

    AtomicReference<GuardTaskPair> task = new AtomicReference<>(null);

    public static boolean has(Guard g) {
        TreeSet<Guard> ts = GuardTask.GUARDS_HELD.get();
        if(ts == null) return false;
        return ts.contains(g);
    }


    public void runGuarded(Runnable r) {
        TreeSet<Guard> tg = new TreeSet<>();
        tg.add(this);
        runGuarded(tg, r);
    }

    public static void runGuarded(Guard g, Runnable r) {
        g.runGuarded(r);
    }
    public static void runGuarded(TreeSet<Guard> gset, Runnable r) {
        GuardTask gt = new GuardTask(gset,r);
        gt.run();
    }

    CondMgr cmgr = new CondMgr();

    public void signal() {
        cmgr.signal();
    }

    public void signalAll() {
        cmgr.signalAll();
    }

    public static <T> void runCondition(
            GuardVar<T> gv,
            final CondArg1<T> c) {
        TreeSet<Guard> ts = new TreeSet<>();
        ts.add(gv);
        Consumer<Future<Boolean>> con = (f)->{ c.run(gv.var, f); };
        runCondition(ts,con);
    }

    public static <T1,T2> void runCondition(
            GuardVar<T1> gv1,
            GuardVar<T2> gv2,
            final CondArg2<T1,T2> c) {
        TreeSet<Guard> ts = new TreeSet<>();
        ts.add(gv1);
        ts.add(gv2);
        Consumer<Future<Boolean>> con = (f)->{ c.run(gv1.var, gv2.var, f); };
        runCondition(ts,con);
    }

    public static <T1,T2,T3> void runCondition(
            GuardVar<T1> gv1,
            GuardVar<T2> gv2,
            GuardVar<T3> gv3,
            final CondArg3<T1,T2,T3> c) {
        TreeSet<Guard> ts = new TreeSet<>();
        ts.add(gv1);
        ts.add(gv2);
        ts.add(gv3);
        Consumer<Future<Boolean>> con = (f)->{ c.run(gv1.var, gv2.var, gv3.var, f); };
        runCondition(ts,con);
    }

    public static void runMe(final List<GuardVar<Object>> vo, final CondArgN c) {
        final List<Var<Object>> objects = new ArrayList<>();
        TreeSet<Guard> ts = new TreeSet<>();
        for(GuardVar<Object> go : vo) {
            ts.add(go);
            objects.add(go.var);
        }
        Consumer<Future<Boolean>> con = (f)->{ c.run(objects, f); };
        runCondition(ts, con);
    }

    public static void runCondition(final TreeSet<Guard> ts,final Consumer<Future<Boolean>> c) {
        assert ts.size() > 0;
        Cond cond = new Cond();
        cond.task = (fb)->{
            Runnable r = ()->{ c.accept(fb); };
            Guard.runGuarded(ts,r);
        };
        for(Guard g : ts) {
            g.cmgr.add(new CondLink(cond));
        }
    }
}

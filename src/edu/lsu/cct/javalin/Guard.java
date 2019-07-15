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

/**
 *
 * @author sbrandt
 */
public class Guard {
    static AtomicInteger idSeq = new AtomicInteger(0);
    public final int id = idSeq.getAndIncrement();
    
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

    public static void runGuarded(TreeSet<Guard> gset, Runnable r) {
        GuardTask gt = new GuardTask(gset,r);
        gt.run();
    }
}

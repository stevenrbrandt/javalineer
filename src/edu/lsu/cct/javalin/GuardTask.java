/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author sbrandt
 */
public class GuardTask {
    static AtomicInteger idSeq = new AtomicInteger(0);
    final int id = idSeq.getAndIncrement();
    
    public String toString() {
        return "GuardTask("+id+")";
    }
    
    public final static GuardTask DONE = new GuardTask(new TreeSet<>(),()->{});
    final List<AtomicReference<GuardTask>> next = new ArrayList<>();

    private final List<Guard> gset = new ArrayList<>();
    private final List<Watcher> watchers = new ArrayList<>();
    private final Runnable r;
    private volatile int index = 0;

    public final static ThreadLocal<TreeSet<Guard>> GUARDS_HELD = new ThreadLocal<>();

    static void run(Runnable r,List<Guard> gset) {
        try {
            TreeSet<Guard> ts = new TreeSet<>();
            ts.addAll(gset);
            assert GUARDS_HELD.get() == null;
            GUARDS_HELD.set(ts);
            r.run();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        } catch(AssertionError e) {
            e.printStackTrace();
            System.exit(2);
        } finally {
            GUARDS_HELD.set(null);
        }
    }

    GuardTask(TreeSet<Guard> gset, Runnable r) {
        this.gset.addAll(gset);
        for(Guard g : gset) {
            next.add(new AtomicReference<>());
        }
        this.r = r;
    }

    GuardTask(TreeSet<Guard> gset, Runnable r, Set<Watcher> gwset) {
        this(gset, r);
        this.watchers.addAll(gwset);
    }
    
    public void run() {
        if(gset.size()==0) {
            run(r,gset);
            decr();
            return;
        }
        int ix = index;
        Guard g = gset.get(ix);
        GuardTaskPair prev = g.task.getAndSet(new GuardTaskPair(this,ix));
        if(prev == null) {
            runTask(g);
        } else {
            assert prev.gtask.next.size() == prev.gtask.gset.size();
            AtomicReference<GuardTask> nextt = prev.gtask.next.get(prev.index);
            assert nextt != null;
            if(!nextt.compareAndSet(null,this)) {
                runTask(g);
            }
            assert nextt.get() != null;
        }
    }
    
    private void free() {
        Guard g = gset.get(index);
        if (!next.get(index).compareAndSet(null, DONE)) {
            GuardTask prev = next.get(index).get();
            assert prev != DONE;
            Pool.run(()->{ prev.runTask(g); });
        }
        assert next.get(index).get() != null;
        int ix = index;
        if (index > 0) {
            index--;
            free();
        }
        if(ix == 0) {
            for (Watcher gw : watchers) {
                gw.decr();
            }
        }
    }

    private void decr() {
        for(Watcher gw : watchers) {
            gw.decr();
        }
    }
    
    private void runTask(Guard g) {
        if(index + 1 == gset.size()) {
            // Don't need to force async, but do
            // so for now.
            Pool.run(()->{
                run(r,gset);
                free();
            });
        } else {
            index++;
            assert index < gset.size();
            Pool.run(()->{ run(); });
        }
    }
}

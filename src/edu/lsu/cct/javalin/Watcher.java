package edu.lsu.cct.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sbrandt
 */
public class Watcher {
    final static AtomicInteger idSeq = new AtomicInteger(1);
    final int id = idSeq.getAndIncrement();
    int count;
    public synchronized void incr() {
        count++;
    }
    public void decr() {
        boolean zero = false;
        synchronized(this) {
            count--;
            zero = (count == 0);
        }
        if(zero) {
            for(Runnable r : tasks)
                Pool.run(r);
            tasks.clear();
        }
    }

    List<Runnable> tasks = new ArrayList<>();

    public void await(Runnable r) {
        synchronized(this) { 
            if(count > 0) {
                tasks.add(r);
                return;
            }
        }
        r.run();
    }
}

package edu.lsu.cct.javalineer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MyPool {
    int busy = 0;

    synchronized void incrBusy() {
        busy++;
    }

    synchronized void decrBusy() {
        busy--;
        assert busy >= 0;
        if (busy == 0)
            notifyAll();
    }

    synchronized void awaitQuiet() {
        while (busy > 0) {
            try {
                wait();
            } catch (InterruptedException ioe) {
            }
        }
    }

    final static AtomicInteger idSeq = new AtomicInteger(0);

    class Worker extends Thread {
        final int id = idSeq.getAndIncrement();

        Worker() {}

        LinkedList<Runnable> ll = new LinkedList<>();

        synchronized void addTask(Runnable t) {
            ll.addLast(t);
            // This worker just became busy
            if(ll.size() == 1) {
                incrBusy();
                // this worker now has tasks...
                notifyAll();
            }
        }

        synchronized Runnable rmTask(boolean[] done) {
            while (ll.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException ioe) {
                }
            }
            Runnable gt = ll.removeFirst();
            done[0] = ll.size() == 0;
            return gt;
        }

        public void run() {
            boolean[] done = new boolean[1];
            try {
                while (true) {
                    Runnable gt = rmTask(done);
                    try {
                        gt.run();
                    } finally {
                        if(done[0])
                            decrBusy();
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    List<Worker> workers = new ArrayList<>();
    final Random RAND = new Random();

    /**
     * Give task to a random worker.
     */
    void add(Runnable gt) {
        int n = RAND.nextInt(workers.size());
        workers.get(n).addTask(gt);
    }

    final int size;
    public MyPool(int size) {
        this.size = size;
        for (int i = 0; i < size; i++) {
            Worker w = this.new Worker();
            w.setDaemon(true);
            workers.add(w);
            w.start();
        }
    }

    public String toString() {
        return "MyPool("+size+")";
    }
}

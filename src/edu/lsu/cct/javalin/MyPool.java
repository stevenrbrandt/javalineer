package edu.lsu.cct.javalin;

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
            notify();
    }

    synchronized void awaitQuiet() {
        while (busy > 0)
            try {
                wait();
            } catch (InterruptedException ioe) {
            }
    }

    final static AtomicInteger idSeq = new AtomicInteger(0);

    class Worker extends Thread {
        final int id = idSeq.getAndIncrement();

        Worker() {
        }

        LinkedList<Runnable> ll = new LinkedList<>();

        synchronized void addTask(Runnable t) {
            ll.addLast(t);
            if(ll.size() == 1) {
                incrBusy();
                notify();
            }
        }

        synchronized Runnable rmTask(boolean[] doDecr) {
            while (ll.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException ioe) {
                }
            }
            Runnable gt = ll.removeFirst();
            doDecr[0] = ll.size() == 0;
            return gt;
        }

        public void run() {
            boolean[] doDecr = new boolean[1];
            try {
                while (true) {
                    Runnable gt = rmTask(doDecr);
                    try {
                        gt.run();
                    } finally {
                        if(doDecr[0])
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

    void add(Runnable gt) {
        int n = RAND.nextInt(workers.size());
        workers.get(n).addTask(gt);
    }

    public MyPool(int size) {
        for (int i = 0; i < size; i++) {
            Worker w = this.new Worker();
            w.setDaemon(true);
            workers.add(w);
            w.start();
        }
    }
}

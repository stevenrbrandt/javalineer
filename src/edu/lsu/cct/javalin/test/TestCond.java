package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;
import java.util.function.Consumer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

class TwoTimes implements Consumer<Future<Boolean>> {
    static AtomicInteger count = new AtomicInteger(0);
    static int lastSuccess;
    int n = 1;
    final int id;
    TwoTimes(int id) { this.id = id; }

    @Override
    public void accept(Future<Boolean> f) {
        count.getAndIncrement();
        if(n-- > 0) {
            System.out.println("id="+id);
            f.set(false);
        } else {
            System.out.println("id="+id+"!");
            f.set(true);
            lastSuccess = id;
        }
    }

    public String toString() {
        return "cond"+id;
    }
}

public class TestCond {
    public static void main(String[] args) {
        Cond c = new Cond();
        final int N = 5;
        for(int i=0;i<N;i++)
            c.add(new TwoTimes(i+1));
        c.signal();
        assert(TwoTimes.lastSuccess == 0);
        TreeSet<Integer> ts = new TreeSet<>();
        for(int i=0;i<N;i++) {
            System.out.println("=======");
            c.signal();
            Pool.await();
            assert(TwoTimes.lastSuccess != 0);
            ts.add(TwoTimes.lastSuccess);
            System.out.println("adding: "+TwoTimes.lastSuccess);
        }
        assert(ts.size()==N);
        System.out.println("ts.size()=="+ts.size());
        for(int i=0;i<N;i++)
            c.add(new TwoTimes(i+1));
        System.out.println("+++++++++++");
        c.signalAll();
        c.signalAll();
        Pool.await();
        assert 4*N == TwoTimes.count.get();
        System.out.println("DONE");
    }
}

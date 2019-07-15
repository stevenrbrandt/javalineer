package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;
import java.util.function.Consumer;
import java.util.TreeSet;

class TwoTimes implements Consumer<Future<Boolean>> {
    static int lastSuccess;
    int n = 1;
    final int id;
    TwoTimes(int id) { this.id = id; }

    @Override
    public void accept(Future<Boolean> f) {
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
        TreeSet<Integer> ts = new TreeSet<>();
        for(int i=0;i<N;i++) {
            System.out.println("=======");
            c.signal();
            Pool.await();
            ts.add(TwoTimes.lastSuccess);
        }
        assert(ts.size()==N);
    }
}

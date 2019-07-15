package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;
import java.util.function.Consumer;

class TwoTimes implements Consumer<Future<Boolean>> {
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
        }
    }

    public String toString() {
        return "cond"+id;
    }
}

public class TestCond {
    public static void main(String[] args) {
        Cond c = new Cond();
        for(int i=0;i<3;i++)
            c.add(new TwoTimes(i+1));
        System.out.println("=======");
        c.signal();
        Pool.await();
        System.out.println("=======");
        c.signal();
        Pool.await();
    }
}

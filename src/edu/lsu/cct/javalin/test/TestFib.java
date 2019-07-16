package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;

public class TestFib {
    static int fibc(int n) {
        if(n < 2) return n;
        return fibc(n-1) + fibc(n-2);
    }

    static Future<Integer> fib(int n) {
        if(n < 2)
            return new Future<Integer>(n);
        Future<Integer> f1 = fib(n-1);
        Future<Integer> f2 = fib(n-2);
        Future<Integer> f = new Future<>();
        f1.then((n1)->{
            f2.then((n2)->{
                f.set(n1.get() + n2.get());
            });
        });
        return f;
    }

    public static void main(String[] args) {
        Test.requireAssert();

        for(int i = 5; i < 27; i++) {
            final int f = i;
            Future<Integer> fib = fib(f);
            fib.then((n)->{
                System.out.printf("fib(%d)=%d%n",f,n.get());
                assert n.get() == fibc(f);
            });
        }

        Pool.await();
    }
}

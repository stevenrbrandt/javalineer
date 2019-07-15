/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalin;

/**
 *
 * @author sbrandt
 */
public class Pool {
    /*
    private static final ForkJoinPool POOL = new ForkJoinPool(4);
    public static void run(Runnable r) {
        POOL.execute(()->{
            try {
                r.run();
            } catch(Throwable t) {
                t.printStackTrace();
                System.exit(0);
            }
        });
    }
    public static void await() {
        POOL.awaitQuiescence(0, TimeUnit.DAYS);
    }*/
    private static final MyPool POOL = new MyPool(Integer.parseInt(System.getProperty("POOL_SIZE","4")));
    public static void await() {
        POOL.awaitQuiet();
    }
    public static void run(Runnable r) {
        POOL.add(r);
    }
}

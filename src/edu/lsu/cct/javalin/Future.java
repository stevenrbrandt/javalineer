/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalin;

import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author sbrandt
 */
public class Future<T> {

    private volatile T data = null;
    private volatile Throwable ex = null;
    private volatile boolean isset = false;

    /**
     * Diagnostic.
     */
    private static boolean complainOnException;

    static {
        complainOnException = System.getProperty("FutExceptionComplain") != null;
        if(complainOnException)
            System.err.println("Fut exception complaining enabled");
    }

    private static void complain(Throwable e) {
        if (!complainOnException) return;
        System.err.println("Future completed exceptionally:");
        e.printStackTrace();
    }

    /**
     * Call to set a data value.
     */
    void set(final T data) {
        assert !this.isset;
        final Future<T> self = this;
        if (data instanceof Future<?>) {
            Future<?> f = (Future<?>)data;
            self.watcher.await(()->{
                self.set(data);
            });
        } else {
            this.data = data;
            this.isset = true;
            watcher.decr();
        }
    }

    /**
     * Call if an exception was thrown.
     */
    void setEx(Throwable ex) {
        assert !this.isset;
        complain(ex);
        this.data = null;
        this.ex = ex;
        this.isset = true;
        watcher.decr();
    }

    public volatile Watcher watcher = new Watcher();

    public T get() { 
        if(!isset)
            throw new Error("Data is not ready");
        if(ex != null)
            throw new RuntimeException(ex);
        return data;
    }

    public Future<Void> await(Runnable r) {
        Future<Void> f = new Future<>();
        f.watcher.incr();
        watcher.await(()->{
            if(ex == null) {
                r.run();
                f.set(null);
            } else {
                f.setEx(ex);
            }
        });
        return f;
    }

    @Override
    public String toString() {
        return "Future [" + (data == null ? "Not completed" : String.valueOf(data)) + "]";
    }
}

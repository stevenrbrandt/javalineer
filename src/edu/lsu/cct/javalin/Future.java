/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalin;

import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author sbrandt
 */
public class Future<T> {
    static class FTask {
        Runnable task;
        AtomicReference<FTask> next = new AtomicReference<>(null);
        public String toString() {
            return "@"+next;
        }
    }

    private volatile T data = null;
    private volatile Throwable ex = null;

    private final static FTask DONE;
    static {
        FTask ft = new FTask();
        ft.next.set(ft);
        DONE = ft;
    }

    private final AtomicReference<FTask> head = new AtomicReference<>(null);
    private final AtomicReference<FTask> tail = new AtomicReference<>(null);

    public Future() {}
    public Future(T t) {
        data = t;
        tail.set(DONE);
        head.set(DONE);
    }

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

    void done() {
        FTask ft = null;
        boolean success = false;
        while(true) {
            ft = tail.get();
            if(ft == null) {
                if(tail.compareAndSet(null,DONE)) {
                    return;
                } else {
                    continue;
                }
            }
            assert ft != DONE;
            if(ft.next.compareAndSet(null,DONE)) {
                success = true;
                break;
            }
        }
        assert success;
        ft = head.get();
        while(ft == null) {
            Thread.yield();
            ft = head.get();
        }
        while(ft != DONE) {
            final Runnable task = ft.task;
            Pool.run(()->{
                task.run();
            });
            ft = ft.next.get();
        }
    }

    /**
     * Call to set a data value.
     */
    @SuppressWarnings("unchecked")
    public void set(final T data) {
        final Future<T> self = this;
        if (data instanceof Future) {
            assert false;
            Future<T> f = (Future<T>)data;
            f.then(()->{
                self.set(f.get());
            });
        } else {
            this.data = data;
            done();
        }
    }

    /**
     * Call if an exception was thrown.
     */
    public void setEx(Throwable ex) {
        complain(ex);
        this.data = null;
        this.ex = ex;
        done();
    }

    public T get() { 
        if(ex != null)
            throw new RuntimeException(ex);
        return data;
    }

    public boolean finished() {
        var ft = tail.get();
        return ft == null || ft.next.get() == DONE;
    }

    public void then(final Runnable r) {
        FTask ft = new FTask();
        ft.task = r;
        while(true) {
            FTask tval = tail.get();
            if(tval == null) {
                if(tail.compareAndSet(null, ft)) {
                    head.set(ft);
                    return;
                } else
                    continue;
            }
            FTask tnext = tval.next.get();
            if(tnext == DONE) {
                Pool.run(()->{ r.run(); });
                return;
            }
            if(tval.next.compareAndSet(null,ft)) {
                while(!tail.compareAndSet(tval,ft))
                    Thread.yield();
                break;
            }
        }
    }

    public void then(Consumer<Future<T>> c) {
        final Future<T> self = this;
        Runnable r = ()->{ c.accept(this); };
        then(r);
    }

    @Override
    public String toString() {
        return "Future [" + (data == null ? "Not completed" : String.valueOf(data)) + "]";
    }
}

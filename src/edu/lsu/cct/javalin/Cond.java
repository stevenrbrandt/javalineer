package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class Cond {
    static enum CondState { ready, busy, finished };
    static class CondList {
        volatile AtomicReference<CondList> next = new AtomicReference<>(null);
        final AtomicReference<CondState> state = new AtomicReference<>(CondState.ready);
        volatile Consumer<Future<Boolean>> task;
        public String toString() {
            return task.toString()+":"+state.get()+"|"+next;
        }
    }
    AtomicReference<CondList> head = new AtomicReference<>(null);

    public void add(Consumer<Future<Boolean>> c) {
        CondList cl = new CondList();
        cl.task = c;
        add(cl);
    }
    private void add(CondList cl) {
        while(true) {
            cl.next.set(head.get());
            if(head.compareAndSet(cl.next.get(), cl))
                break;
        }
        //Here.println(head.get());
    }

    private void addBack(CondList cl) {
        cl.state.set(CondState.ready);
    }

    public void signal() {
        //Here.println("head: "+head.get());
        signal(head.get());
    }

    private void signal(CondList cl) {
        while(cl != null) {
            //Here.println("signal: "+cl);
            if(cl.state.compareAndSet(CondState.ready, CondState.busy)) {
                final CondList cf = cl;
                Future<Boolean> f = new Future<>();
                f.then((b)->{
                    if(b.get()) {
                        cf.state.compareAndSet(CondState.busy, CondState.finished);
                    } else {
                        cf.state.compareAndSet(CondState.busy, CondState.ready);
                        signal(cf.next.get());
                    }
                });
                cl.task.accept(f);
                break;
            }
            cl = cl.next.get();
        }
    }

    public void signalAll() {
        CondList cl = head.get();
        while(cl != null) {
            //Here.println("all: "+cl);
            if(cl.state.compareAndSet(CondState.ready, CondState.busy)) {
                final CondList cf = cl;
                Future<Boolean> f = new Future<>();
                f.then((b)->{
                    if(b.get()) {
                        cf.state.compareAndSet(CondState.busy, CondState.finished);
                    } else {
                        cf.state.compareAndSet(CondState.busy, CondState.ready);
                    }
                });
                cl.task.accept(f);
            }
            cl = cl.next.get();
        }
    }
}

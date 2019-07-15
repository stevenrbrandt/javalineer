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

    private CondList getNext(AtomicReference<CondList> cl) {
        CondList next = null;
        while(true) {
            next = cl.get();
            if(next == null)
                return null;
            if(next.state.get() == CondState.finished) {
                CondList nx = next.next.get();
                cl.compareAndSet(next, nx);
            } else
                break;
        }
        return next;
    }

    public void signal() {
        CondList cl = getNext(head);
        signal(cl);
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
            cl = getNext(cl.next);
        }
    }

    public void signalAll() {
        CondList cl = getNext(head);
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
            cl = getNext(cl.next);
        }
    }
}

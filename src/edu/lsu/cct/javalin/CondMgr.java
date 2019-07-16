package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class CondMgr {
    AtomicReference<CondLink> head = new AtomicReference<>(null);

    public void add(Consumer<Future<Boolean>> c) {
        CondLink cl = new CondLink(new Cond());
        cl.cond.task = c;
        add(cl);
    }
    public void add(CondLink cl) {
        while(true) {
            cl.next.set(head.get());
            if(head.compareAndSet(cl.next.get(), cl))
                break;
        }
        //Here.println(head.get());
    }

    private void addBack(CondLink cl) {
        cl.cond.state.set(CondState.ready);
    }

    private CondLink getRef(AtomicReference<CondLink> ref) {
        CondLink r = null;
        while(true) {
            r = ref.get();
            if(r == null)
                return null;
            if(r.cond.state.get() == CondState.finished) {
                CondLink r2 = r.next.get();
                ref.compareAndSet(r, r2);
            } else
                break;
        }
        return r;
    }

    public void signal() {
        CondLink cl = getRef(head);
        signal(cl);
    }

    private void signal(CondLink cl) {
        while(cl != null) {
            //Here.println("signal: "+cl);
            if(cl.cond.state.compareAndSet(CondState.ready, CondState.busy)) {
                final CondLink cf = cl;
                Future<Boolean> f = new Future<>();
                f.then((b)->{
                    if(b.get()) {
                        cf.cond.state.compareAndSet(CondState.busy, CondState.finished);
                    } else {
                        cf.cond.state.compareAndSet(CondState.busy, CondState.ready);
                        signal(cf.next.get());
                    }
                });
                cl.cond.task.accept(f);
                break;
            }
            cl = getRef(cl.next);
        }
    }

    public void signalAll() {
        CondLink cl = getRef(head);
        while(cl != null) {
            //Here.println("all: "+cl);
            if(cl.cond.state.compareAndSet(CondState.ready, CondState.busy)) {
                final CondLink cf = cl;
                Future<Boolean> f = new Future<>();
                f.then((b)->{
                    if(b.get()) {
                        cf.cond.state.compareAndSet(CondState.busy, CondState.finished);
                    } else {
                        cf.cond.state.compareAndSet(CondState.busy, CondState.ready);
                    }
                });
                cl.cond.task.accept(f);
            }
            cl = getRef(cl.next);
        }
    }
}
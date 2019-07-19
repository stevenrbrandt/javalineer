package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class CondMgr {
    AtomicReference<CondLink> head = new AtomicReference<>(null);

    public void add(CondTask c) {
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
    }

    private void addBack(CondLink cl) {
        cl.cond.state.set(Cond.READY);
    }

    private CondLink getRef(AtomicReference<CondLink> ref) {
        CondLink r = null;
        while(true) {
            r = ref.get();
            if(r == null)
                return null;
            if(r.cond.state.get() == Cond.FINISHED) {
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
        if(cl != null) {
            final CondLink cf = cl;
            final CondTask task = cl.cond.task;
            Guard.runGuarded(cl.cond.gset,()->{
                task.run();
                if(!task.done)
                    signal(getRef(cf.next));
            });
        }
    }

    public void signalAll() {
        CondLink cl = getRef(head);
        while(cl != null) {
            Guard.runGuarded(cl.cond.gset, cl.cond.task);
            cl = getRef(cl.next);
        }
    }
}

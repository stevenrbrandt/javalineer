package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.TreeSet;


public class CondMgr {
    AtomicReference<CondLink> head = new AtomicReference<>(null);

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
            if(r.cond.task.done) {
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
            if(cl.cond == null) throw cl.err;
            assert cl.cond.gset != null;
            final CondLink cf = cl;
            final CondTask task = cl.cond.task;
            Guard.runGuarded(cl.cond.gset,()->{
                if(task.done) {
                    signal(getRef(cf.next));
                    ;//Here.println(" signal re-run "+cf.cond);
                    return;
                }
                task.run();
                if(task.done)
                    ;//Here.println(" signal run "+cf.cond);
                else
                    ;//Here.println(" signal fail "+cf.cond);
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

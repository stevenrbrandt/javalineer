package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Cond {
    static class CondList {
        volatile CondList next;
        volatile Consumer<Future<Boolean>> task;
        public String toString() {
            return task.toString()+"|"+next;
        }
    }
    static class Pile {
        volatile CondList in, out;
        public String toString() {
            return "{"+in+"}{"+out+"}";
        }
    }
    AtomicReference<Pile> pile = new AtomicReference<>(new Pile());

    public void add(Consumer<Future<Boolean>> c) {
        CondList cl = new CondList();
        cl.task = c;
        add(cl);
    }
    private void add(CondList cl) {
        while(true) {
            Pile p = pile.get();
            Pile p2 = new Pile();
            cl.next = p.in;
            p2.in = cl;
            p2.out = p.out;
            if(pile.compareAndSet(p,p2)) {
                break;
            }
        }
    }

    private void addBack(CondList cl) {
        while(true) {
            Pile p = pile.get();
            Pile p2 = new Pile();
            cl.next = p.out;
            p2.in = p.in;
            p2.out = cl;
            if(pile.compareAndSet(p,p2))
                break;
        }
    }

    public CondList remove() {
        while(true) {
            Pile p = pile.get();
            //Here.println("p="+p);
            Pile p2 = new Pile();
            if(p.in == null) {
                if(p.out == null) {
                    return null;
                }
                CondList ret = p.out;
                p2.in = ret.next;
                p2.out = null;
                if(pile.compareAndSet(p,p2))
                    return ret;
            } else {
                CondList ret = p.in;
                p2.in = ret.next;
                p2.out = p.out;
                if(pile.compareAndSet(p,p2))
                    return ret;
            }
        }
    }

    public void signal() {
        CondList cl = remove();
        subSignal(cl,cl);
    }

    private void subSignal(CondList cl, CondList end) {
        if(cl == null)
            return;
        Future<Boolean> f = new Future<>();
        f.then((b)->{
            boolean ans = b.get();
            if(!ans) {
                addBack(cl);
                CondList cn = remove();
                if(cn != end)
                    subSignal(cn,end);
            }
        });
        cl.task.accept(f);
    }

    public void signalAll() {
        Pile p = pile.getAndSet(new Pile());
        while(p.in != null) {
            CondList cl = p.in;
            p.in = p.in.next;
            subSignalAll(cl);
        }
        while(p.out != null) {
            CondList cl = p.out;
            p.out = p.out.next;
            subSignalAll(cl);
        }
    }

    private void subSignalAll(CondList cl) {
        Future<Boolean> f = new Future<>();
        f.then((b)->{
            if(!b.get()) {
                addBack(cl);
            }
        });
        cl.task.accept(f);
    }
}

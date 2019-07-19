package edu.lsu.cct.javalin.test;

import java.util.*;
import edu.lsu.cct.javalin.*;

public class March2 {
    static class Segment {
        GuardVar<Segment> left, right;
        int step;
        final int id;
        Segment() { this.id = -1; step = 10; }
        Segment(int id) { this.id = id; }
        public String toString() {
            return "Seg("+id+","+step+")";
        }
    }

    public static void main(String[] args) {
        List<GuardVar<Segment>> line = new ArrayList<>();
        final int N_SEGS = 10;
        for(int i=0;i<N_SEGS;i++) {
            line.add(new GuardVar<>(new Segment(i)));
        }
        for(int i=1;i<N_SEGS-1;i++) {
            final int ix = i;
            Guard.runGuarded(line.get(i),(s)->{
                s.get().left = line.get(ix-1);
                s.get().right = line.get(ix+1);
            });
        }
        GuardVar<Segment> send = new GuardVar<>(new Segment());
        Guard.runGuarded(line.get(0),(s)->{
            s.get().left = send;
            s.get().right = line.get(1);
        });
        Guard.runGuarded(line.get(N_SEGS-1),(s)->{
            s.get().left = line.get(N_SEGS-2);
            s.get().right = send;
        });
        Pool.await();
        for(int i=0;i<N_SEGS;i++) {
            final int ix = i;
            final GuardVar<Segment> me = line.get(i);
            Guard.runGuarded(line.get(i),(s)->{
                final GuardVar<Segment> left = s.get().left;
                final GuardVar<Segment> right = s.get().right;
                Guard.runCondition(me, left, right, (ss,l,r,f)->{
                    final int lstep = l.get().step;
                    final int rstep = r.get().step;
                    final int step = ss.get().step;
                    if(lstep >= step && rstep >= step) {
                        ss.get().step++;
                        if(ss.get().id==0)
                            Here.println("id: "+ss.get().id+", step: "+ss.get().step+"/"+rstep+"/"+lstep+"/"+step);
                        if(ss.get().id > 0)
                            left.signal();
                        if(ss.get().id < N_SEGS-1)
                            right.signal();
                        f.set(true);
                        return;
                    }
                    f.set(false);
                });
            });
        }
        Pool.await();
    }
}

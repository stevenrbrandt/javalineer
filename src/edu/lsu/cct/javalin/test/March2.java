package edu.lsu.cct.javalin.test;

import java.util.*;
import edu.lsu.cct.javalin.*;
import java.util.concurrent.atomic.AtomicInteger;

public class March2 implements Runnable {
    final static int N_SEGS = 10;
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
    static class Stepper extends CondTask3<Segment,Segment,Segment> {
      public boolean check(Var<Segment> l,Var<Segment> ss,Var<Segment> r) {
        final int lstep = l.get().step;
        final int rstep = r.get().step;
        final int step = ss.get().step;
        //new Throwable("runstep").printStackTrace();
        if(lstep >= step && rstep >= step) {
            /*
            if(ss.get().id == 0 || ss.get().id == N_SEGS-1) {
                ss.get().step = 10;
                return true;
            }
            */
            ss.get().step++;
            Stepper stepper = new Stepper();
            assert !stepper.isDone();
            Guard.runCondition(l.guardVar(), ss.guardVar(), r.guardVar(), stepper);
            boolean show = ss.get().id == 0;// || ss.get().id == N_SEGS/2;
            if(show) 
                Here.print("id: "+ss.get().id+", step: s="+ss.get().step+"/r="+rstep+"/l="+lstep);
            if(ss.get().id > 0) {
                l.signal();
                if(ss.get().id==0)
                    System.out.print("  left:"+l.get().id);
            }
            if(ss.get().id < N_SEGS-1) {
                r.signal();
                if(show)
                    System.out.print("  right:"+r.get().id);
            }
            if(show)
                System.out.println();
            return true;
        } else {
            //Here.println(" -> id: "+ss.get().id+", step: s="+ss.get().step+"/r="+rstep+"/l="+lstep);
        }
        return false;
    }
  }

    public void run() {
        List<GuardVar<Segment>> line = new ArrayList<>();
        for(int i=0;i<N_SEGS;i++) {
            line.add(new GuardVar<>(new Segment(i)));
        }
        final GuardVar<Segment> send = new GuardVar<>(new Segment());
        final AtomicInteger ai = new AtomicInteger(0);
        for(int i=0;i<N_SEGS;i++) {
            final int ix = i;
            Guard.runGuarded(line.get(i),(s)->{
                int i0 = ix - 1;
                int iN = ix + 1;
                if(i0 < 0)
                    s.get().left = send;
                else
                    s.get().left = line.get(i0);
                if(iN >= line.size())
                    s.get().right = send;
                else
                    s.get().right = line.get(iN);
                int nv = ai.incrementAndGet();
                var rstep = s.get().right;
                var lstep = s.get().left;
                Here.println("nv="+nv);
                if(nv == N_SEGS) {
                    for(int j=0;j<N_SEGS;j++) {
                        Guard.runGuarded(line.get(j),(ss)->{
                            Guard.runCondition(ss.get().left,ss.guardVar(),ss.get().right,new Stepper());
                        });
                    }
                }
            });
        }
        /*
        for(int i=0;i<N_SEGS;i++) {
            final int ix = i;
            final GuardVar<Segment> me = line.get(i);
            Guard.runGuarded(line.get(i),new GuardArg1<Segment>() {
              public void run(Var<Segment> s) {
                final GuardVar<Segment> left = s.get().left;
                final GuardVar<Segment> right = s.get().right;
                Guard.runCondition(me, left, right, new CondTask3<>() {
                  public boolean check(Var<Segment> ss,Var<Segment> l,Var<Segment> r) {
                    final int lstep = l.get().step;
                    final int rstep = r.get().step;
                    final int step = ss.get().step;
                    if(lstep >= step && rstep >= step) {
                        ss.get().step++;
                        if(true)//(ss.get().id==0)
                            Here.print("id: "+ss.get().id+", step: s="+ss.get().step+"/r="+rstep+"/l="+lstep);
                        if(ss.get().id > 0) {
                            l.signal();
                            System.out.print("  left:"+l.get().id);
                        }
                        if(ss.get().id < N_SEGS-1) {
                            r.signal();
                            System.out.print("  right:"+r.get().id);
                        }
                        System.out.println();
                        return true;
                    } else {
                        Here.println(" -> id: "+ss.get().id+", step: s="+ss.get().step+"/r="+rstep+"/l="+lstep);
                    }
                    return false;
                }
              });
            }
          });
        }
        */
      }

      public static void main(String[] args) {
        Pool.run(new March2());
        Pool.await();
      }
}
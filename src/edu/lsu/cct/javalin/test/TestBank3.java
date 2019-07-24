package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBank3 {
    static AtomicInteger wc = new AtomicInteger(0), tc = new AtomicInteger(0), dc = new AtomicInteger(0);

    static class Bank extends Guarded {
        int balance = 0;

        boolean withdraw(int a) {
            assert a > 0;
            if(a > balance)
                return false;
            balance -= a;
            return true;
        }

        void deposit(int a) {
            assert a > 0;
            balance += a;
        }
    }

    public static void main(String[] args) {
        Test.requireAssert();

        GuardVar<Bank> a = new GuardVar<>(new Bank());
        GuardVar<Bank> b = new GuardVar<>(new Bank());

        for(int i=0;i<1000;i++) {
            Pool.run(()->{
                Guard.runCondition(a,new CondTask1<>() {
                    public boolean check(Var<Bank> bank) {
                        boolean b = bank.get().withdraw(1);
                        if(b) wc.getAndIncrement();
                        return b;
                    }
                });
            });
            Pool.run(()->{
                Guard.runCondition(a,b,new CondTask2<>() {
                    public boolean check(Var<Bank> banka,Var<Bank> bankb) {
                        if(bankb.get().withdraw(1)) {
                            banka.get().deposit(1);
                            banka.signal();
                            tc.getAndIncrement();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            });
            Pool.run(()->{
                Guard.runGuarded(b,new GuardTask1<>() {
                    public void run(Var<Bank> bank) {
                        bank.get().deposit(1);
                        bank.signal();
                        dc.getAndIncrement();
                    }
                });
            });
        }

        Pool.await();
        int[] out = new int[1];
        System.out.println("wc="+wc+", tc="+tc+", dc="+dc);

        a.runGuarded((bank)->{
            out[0] = bank.get().balance;
            assert out[0] == 0 : out[0];
        });

        Pool.await();

    }
}

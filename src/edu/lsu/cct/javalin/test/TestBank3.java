package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;

public class TestBank3 {

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
                Guard.runCondition(a,(bank,fb)->{
                    fb.set(bank.get().withdraw(1));
                });
            });
            Pool.run(()->{
                Guard.runCondition(a,b,(banka,bankb,fb)->{
                    if(bankb.get().withdraw(1)) {
                        banka.get().deposit(1);
                        banka.get().getGuard().signal();
                        fb.set(true);
                    } else {
                        fb.set(false);
                    }
                });
            });
            Pool.run(()->{
                b.runGuarded((bank)->{
                    bank.get().deposit(1);
                    bank.get().getGuard().signal();
                });
            });
        }

        Pool.await();
        int[] out = new int[1];

        a.runGuarded((bank)->{
            out[0] = bank.get().balance;
            assert out[0] == 0;
        });

        Pool.await();

    }
}

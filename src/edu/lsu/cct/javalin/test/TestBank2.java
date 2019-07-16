package edu.lsu.cct.javalin.test;

import edu.lsu.cct.javalin.*;

public class TestBank2 {

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

        for(int i=0;i<1000;i++) {
            Pool.run(()->{
                Guard.runCondition(a,(bank,fb)->{
                    fb.set(bank.get().withdraw(1));
                });
            });
            Pool.run(()->{
                a.runGuarded((bank)->{
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

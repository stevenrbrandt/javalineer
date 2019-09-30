package edu.lsu.cct.javalineer.test;

import edu.lsu.cct.javalineer.*;

public class TestBank {

    static class Bank {
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

    static int failures = 0;

    public static void main(String[] args) {
        Test.requireAssert();

        GuardVar<Bank> a = new GuardVar<>(new Bank());

        for(int i=0;i<1000;i++) {
            Pool.run(()->{
                a.runGuarded((bank)->{
                    if(!bank.get().withdraw(1))
                        failures++;
                });
            });
            Pool.run(()->{
                a.runGuarded((bank)->{
                    bank.get().deposit(1);
                    bank.signal();
                });
            });
        }

        Pool.await();
        int[] out = new int[1];

        a.runGuarded((bank)->{
            out[0] = bank.get().balance;
            assert out[0] == failures;
        });

        Pool.await();

    }
}

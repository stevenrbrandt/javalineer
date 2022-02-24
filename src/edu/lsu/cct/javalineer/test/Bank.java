package edu.lsu.cct.javalineer.test;

import edu.lsu.cct.javalineer.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import edu.lsu.cct.javalineer.Guard;
import edu.lsu.cct.javalineer.GuardVar;
import java.util.concurrent.*;
//import edu.lsu.cct.javalineer.CompletableFuture;

import java.io.PrintStream;


public class Bank {
    public static class Printy {}
    public final String name;
    public Bank(String name) {
        this.name = name;
    }

    private final static GuardVar<Printy> printer = new GuardVar<>(new Printy());
    private static void print(String s) {
        Guard.runGuarded(printer, (pr)->{
        	System.out.println(s);
        });
    }
    private final GuardVar<Double> balance = new GuardVar<>(0.0);

    public CompletableFuture<Void> deposit(int amount) {
        assert amount > 0;
	    CompletableFuture<Void> done = new CompletableFuture<>();
        Guard.runGuarded(balance,(b)->{
        	b.set(b.get() + amount);
        	print(String.format("deposit: %s %.2f",this.name,b.get()));
        	balance.signal();
            done.complete(null);
		});
        return done;
    }

    /**
     * If the amount is insufficient, it
     * waits until it is.
     */
    public void withdraw(int amount) {
        assert amount > 0;
        Guard.runCondition(balance,(b)->{
            if(amount > b.get())
                return false;
            b.set(b.get() - amount);
            print(String.format("withdraw: %s %.2f",name,b.get()));
            return true;
        });
    }

    public static CompletableFuture<Void> withdrawFromLarger(Bank b1, Bank b2, double amount) {
	    CompletableFuture<Void> done = new CompletableFuture<>();
        Guard.runCondition(b1.balance,b2.balance,(bb1, bb2)->{
            if (bb1.get() >= bb2.get() && amount < bb1.get()) {
                bb1.set(bb1.get() - amount);
                print("Decr b1");
                return true;
            } else if (bb2.get() >= bb1.get() && amount < bb2.get()) {
                bb2.set(bb2.get() - amount);
                print(String.format("Decr b2 = %.2f", bb2.get()));
                done.complete(null);
                return true;
            } else {
                print("wait...");
                return false;
            }
        });
        return done;
    }

    public CompletableFuture<Double> getBalance() {
	    CompletableFuture<Double> done = new CompletableFuture<>();
        Guard.runGuarded(this.balance,(b)->{
            done.complete(b.get());
        });
        return done;
    }

    public static void main(String[] args) {
        //Tripwire.incr(); // atomic static must be zero at the end
        Bank b1 = new Bank("Joe");
        Bank b2 = new Bank("Fred");
        b1.deposit(1000);
        b2.deposit(1000);
        b1.withdraw(1100);
        b1.deposit(200);
        var v1 = withdrawFromLarger(b1,b2,10000);
        b2.deposit(20000);
        v1.thenAccept((v1_)->{
            b1.getBalance().thenAccept((a)->{
                b2.getBalance().thenAccept((b)->{
                    print(String.format("b1 = %.2f, b2 = %.2f", a, b));
                    //Tripwire.decr();
                    print("Done");
                });
            });
        });
    }
}

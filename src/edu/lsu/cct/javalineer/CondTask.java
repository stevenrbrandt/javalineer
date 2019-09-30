package edu.lsu.cct.javalineer;

public abstract class CondTask implements Runnable {

    volatile boolean done = false;

    public abstract void run();
    
    public final boolean isDone() { return done; }
}

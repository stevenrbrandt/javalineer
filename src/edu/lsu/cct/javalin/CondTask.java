package edu.lsu.cct.javalin;

public abstract class CondTask implements Runnable {

    volatile boolean done = false;

    public abstract void run();
    
    public final boolean isDone() { return done; }
}

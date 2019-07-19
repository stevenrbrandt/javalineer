package edu.lsu.cct.javalin;

public abstract class CondTask implements Runnable {
    public abstract boolean check();

    boolean done = false;

    public void run() {
        if(!done)
            done = check();
    }
}

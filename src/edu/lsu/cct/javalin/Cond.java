package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Cond {
    // 0 -> ready
    // 1 -> complete
    // 2+ -> busy
    final static int READY = 0, FINISHED = 1, BUSY = 2;
    final AtomicInteger state = new AtomicInteger(0);
    volatile Consumer<Future<Boolean>> task;
    public String toString() {
        return task.toString()+":"+state.get();
    }
}

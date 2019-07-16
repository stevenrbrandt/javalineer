package edu.lsu.cct.javalin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CondLink {
    volatile AtomicReference<CondLink> next = new AtomicReference<>(null);
    final Cond cond;
    public CondLink(Cond c) { cond = c; }
    public String toString() {
        return cond.toString()+"|"+next;
    }
}

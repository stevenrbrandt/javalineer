package edu.lsu.cct.javalin;

import java.util.List;

public interface CondArgN {
    void run(List<Var<Object>> v,Future<Boolean> fb);
}

package edu.lsu.cct.javalineer;

public class Val<T> {
    T data;
    Throwable ex;

    Val(T t,Throwable ex) { data = t; this.ex = ex; }

    public T get() {
        if(ex != null)
            throw new RuntimeException(ex);
        return data;
    }
}

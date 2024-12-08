package org.test;

public class Test {

    public static void main(String[] args) {
        String a = args[0];
        Wrapper w = new Wrapper();
        Base o = new Sink();
        w.setF(o);
        w.call(a);
    }

}
class Wrapper {
    private Base f;
    public void setF(Base f) {
        this.f = f;
    }
    public void call(String a) {
        f.foo(a);
    }
}
class Base {
    public void foo(String a) {
        System.out.println("Base");
    }
}
class A extends Base {
    public void foo(String a) {
        System.out.println("A");
    }
}
class Sink extends Base {
    public void foo(String s) {
        SinkClass sc = new SinkClass();
        sc.anInstanceSink(s);
    }
}

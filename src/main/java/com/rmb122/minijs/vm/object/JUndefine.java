package com.rmb122.minijs.vm.object;

public class JUndefine implements JBaseObject {
    public static final JUndefine UNDEFINE = new JUndefine();

    private JUndefine() {
    }

    @Override
    public JNumber toJNumber() {
        return JNumber.NAN;
    }

    @Override
    public JString toJString() {
        return new JString("undefined");
    }
}

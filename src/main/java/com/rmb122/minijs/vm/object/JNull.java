package com.rmb122.minijs.vm.object;

public class JNull implements JBaseObject {
    public static final JNull NULL = new JNull();

    private JNull() {
    }

    @Override
    public JNumber toJNumber() {
        return new JNumber(0);
    }

    @Override
    public JString toJString() {
        return new JString("null");
    }
}

package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.JException;

public class JBoolean implements JBaseObject {
    public static final JBoolean TRUE = new JBoolean();
    public static final JBoolean FALSE = new JBoolean();

    private JBoolean() {
    }

    public static JBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static JBoolean valueOf(JNumber value) {
        return JBoolean.valueOf(value.getValue() > 0);
    }

    @Override
    public JNumber toJNumber() throws JException {
        if (this == TRUE) {
            return new JNumber(1);
        } else if (this == FALSE) {
            return new JNumber(0);
        } else {
            throw new JException("invalid JBoolean value");
        }
    }

    @Override
    public JString toJString() throws JException {
        if (this == TRUE) {
            return new JString("true");
        } else if (this == FALSE) {
            return new JString("false");
        } else {
            throw new JException("invalid JBoolean value");
        }
    }

    public boolean toPrimitive() {
        return this == TRUE;
    }
}

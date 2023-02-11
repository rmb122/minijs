package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.lexer.TokenValue;

public class JNumber implements JBaseObject {
    private final double value;
    public static final JNumber NAN = new JNumber(Double.NaN);

    public JNumber(double value) {
        this.value = value;
    }

    @Override
    public JNumber toJNumber() {
        return this;
    }

    @Override
    public JString toJString() {
        return new JString(Double.toString(this.value));
    }

    public static JNumber fromTokenValue(String tokenValue) {
        return new JNumber(Double.parseDouble(tokenValue));
    }
}

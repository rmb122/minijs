package com.rmb122.minijs.vm.object;

import java.util.Objects;
import java.util.Stack;

public class JNumber implements JBaseObject {
    private final double value;
    public static final JNumber NAN = new JNumber(Double.NaN);

    public JNumber(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public JNumber toJNumber() {
        return this;
    }

    @Override
    public JString toJString() {
        if (Math.ceil(this.value) == Math.floor(this.value)) {
            return new JString(Long.toString((long) this.value));
        } else {
            return new JString(String.format("%f", this.value));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JNumber jNumber && this.value == jNumber.getValue();
    }

    public static JNumber fromTokenValue(String tokenValue) {
        return new JNumber(Double.parseDouble(tokenValue));
    }
}

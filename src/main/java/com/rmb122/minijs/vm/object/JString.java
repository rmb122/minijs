package com.rmb122.minijs.vm.object;

import org.apache.commons.text.StringEscapeUtils;

public class JString implements JBaseObject {
    private final String value;

    public JString(String value) {
        this.value = value;
    }

    @Override
    public JNumber toJNumber() {
        if (this.value.length() == 0) {
            return new JNumber(0);
        }

        try {
            return new JNumber(Double.parseDouble(this.toString()));
        } catch (NumberFormatException e) {
            return JNumber.NAN;
        }
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JString jString && this.value.equals(jString.value);
    }

    @Override
    public JString toJString() {
        return this;
    }

    public String toString() {
        return this.value;
    }

    public static JString fromTokenValue(String tokenValue) {
        return new JString(StringEscapeUtils.unescapeEcmaScript(tokenValue.substring(1, tokenValue.length() - 1)));
    }
}

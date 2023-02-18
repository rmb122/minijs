package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.JException;

import java.util.HashMap;
import java.util.Objects;

public class JObject implements JBaseObject {
    private final HashMap<JString, JBaseObject> values = new HashMap<>();

    @Override
    public JBaseObject get(JString name) {
        JBaseObject v = values.get(name);
        return Objects.requireNonNullElse(v, JUndefine.UNDEFINE);
    }

    @Override
    public JBaseObject set(JString name, JBaseObject value) throws JException {
        return values.put(name, value);
    }

    @Override
    public JBaseObject remove(JString name) {
        return values.remove(name);
    }

    @Override
    public JNumber toJNumber() {
        return JNumber.NAN;
    }

    @Override
    public JString toJString() {
        return new JString("[object Object]");
    }
}

package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;

import java.util.List;

public interface JBaseObject {
    default JBaseObject get(JString name) throws JException {
        throw new JException(String.format("Cannot read properties of %s (reading %s)", this.toJString(), name));
    }

    default void set(JString name, JBaseObject value) throws JException {
        throw new JException(String.format("Cannot set properties of %s (setting %s)", this.toJString(), name));
    }

    default void remove(JString name) throws JException {
        throw new JException(String.format("Cannot remove properties of %s (removing %s)", this.toJString(), name));
    }

    default JBaseObject call(Context context, JBaseObject thisObject, List<JBaseObject> args) throws JException {
        throw new JException(String.format("%s is not a function", this.toJString()));
    }

    JNumber toJNumber() throws JException;

    JString toJString() throws JException;
}

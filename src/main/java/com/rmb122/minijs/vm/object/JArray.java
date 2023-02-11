package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.JException;

import java.util.ArrayList;

public class JArray extends JObject {
    public static final JString LENGTH_KEY = new JString("length");
    private final ArrayList<JBaseObject> array = new ArrayList<>();

    @Override
    public JBaseObject set(JString name, JBaseObject value) throws JException {
        if (name.equals(LENGTH_KEY)) {
            try {
                Long length = Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                throw new JException("Invalid array length");
            }
        }

        return super.set(name, value);
    }

    @Override
    public JBaseObject get(JString name) {
        return super.get(name);
    }

    public int length() {
        return this.array.size();
    }

    public void push(JBaseObject jObject) {
        this.array.add(jObject);
    }
}

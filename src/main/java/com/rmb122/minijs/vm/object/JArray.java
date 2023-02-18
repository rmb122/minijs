package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.JException;

import java.util.ArrayList;
import java.util.List;

public class JArray extends JObject {
    public static final JString LENGTH_KEY = new JString("length");
    private List<JBaseObject> array = new ArrayList<>();

    private void adjustLength(int length) throws JException {
        if (length < 0) {
            throw new JException("Invalid array length");
        }

        int selfLength = this.length();
        if (length > selfLength) {
            for (int i = selfLength; i < length; i++) {
                this.push(JUndefine.UNDEFINE);
            }
        } else if (length < selfLength) {
            this.array = this.array.subList(0, length);
        }
    }

    @Override
    public JBaseObject set(JString name, JBaseObject value) throws JException {
        if (name.equals(LENGTH_KEY)) {
            try {
                int length = Integer.parseInt(value.toString());
                this.adjustLength(length);
                return value;
            } catch (NumberFormatException e) {
                throw new JException("Invalid array length");
            }
        }

        try {
            int index = Integer.parseInt(name.toString());
            this.adjustLength(index + 1);
            return this.array.set(index, value);
        } catch (NumberFormatException e) {
            return super.set(name, value);
        }
    }

    @Override
    public JBaseObject get(JString name) {
        if (name.equals(LENGTH_KEY)) {
            return new JNumber(this.length());
        }

        try {
            int index = Integer.parseInt(name.toString());
            if (index < this.length()) {
                return this.array.get(index);
            } else {
                return JUndefine.UNDEFINE;
            }
        } catch (NumberFormatException e) {
            return super.get(name);
        }
    }

    public int length() {
        return this.array.size();
    }

    public void push(JBaseObject jObject) {
        this.array.add(jObject);
    }
}

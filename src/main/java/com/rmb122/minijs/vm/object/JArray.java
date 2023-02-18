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
    public void set(JString name, JBaseObject value) throws JException {
        if (name.equals(LENGTH_KEY)) {
            try {
                int length = Integer.parseInt(value.toJString().toString());
                this.adjustLength(length);
            } catch (NumberFormatException e) {
                throw new JException("Invalid array length");
            }
        }

        try {
            int index = Integer.parseInt(name.toString());

            if (index >= 0) {
                if (index + 1 > this.length()) {
                    this.adjustLength(index + 1);
                }
                this.array.set(index, value);
                return;
            }
        } catch (NumberFormatException ignored) {
        }

        super.set(name, value);
    }

    @Override
    public JBaseObject get(JString name) {
        if (name.equals(LENGTH_KEY)) {
            return new JNumber(this.length());
        }

        try {
            int index = Integer.parseInt(name.toString());
            if (index >= 0) {
                if (index < this.length()) {
                    return this.array.get(index);
                } else {
                    return JUndefine.UNDEFINE;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        return super.get(name);
    }

    @Override
    public void remove(JString name) {
        if (name.equals(LENGTH_KEY)) {
            return;
        }

        try {
            int index = Integer.parseInt(name.toString());
            if (index >= 0) {
                if (index < this.length()) {
                    this.array.set(index, JUndefine.UNDEFINE);
                }
                return;
            }
        } catch (NumberFormatException ignored) {
        }

        super.remove(name);
    }

    public int length() {
        return this.array.size();
    }

    public void push(JBaseObject jObject) {
        this.array.add(jObject);
    }
}

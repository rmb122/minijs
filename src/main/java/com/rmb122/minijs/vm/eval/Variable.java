package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JUndefine;

public class Variable extends LeftHandExpr {
    public enum Type {
        INSTANT,
        IDENTIFIER,
        THIS
    }

    public static final Variable THIS = new Variable(Type.THIS);

    // 当前变量类型
    Type type;
    JBaseObject jObject;
    String id;

    public Variable(JBaseObject jObject) {
        this.type = Type.INSTANT;
        this.jObject = jObject;
    }

    public Variable(String id) {
        this.type = Type.IDENTIFIER;
        this.id = id;
    }

    private Variable(Type type) {
        this.type = type;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        switch (this.type) {
            case THIS -> {
                return context.currFrame.thisObject;
            }
            case INSTANT -> {
                return this.jObject;
            }
            case IDENTIFIER -> {
                if (context.currFrame.variables.containsKey(id)) {
                    return context.currFrame.variables.get(id);
                } else {
                    return context.globalThis.getOrDefault(id, JUndefine.UNDEFINE);
                }
            }
            default -> throw new JException("Unexpected var type");
        }
    }
}

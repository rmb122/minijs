package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;

public class Variable implements Expr {
    public enum Type {
        INSTANT,
        IDENTIFIER,
        THIS
    }

    public static final Variable THIS = new Variable(Type.THIS);

    // 当前变量类型
    private final Type type;
    private JBaseObject jObject;
    private String id;

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

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        switch (this.type) {
            case THIS -> {
                return context.getThis();
            }
            case INSTANT -> {
                return this.jObject;
            }
            case IDENTIFIER -> {
                return context.getVariable(this.id);
            }
            default -> throw new JException("Unexpected var type");
        }
    }
}

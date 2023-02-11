package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JObject;
import com.rmb122.minijs.vm.object.JString;

import java.util.HashMap;

public class ObjectLiteralExpr extends Expr {
    private final HashMap<String, Expr> values = new HashMap<>();

    public void addLiteral(String name, Expr value) {
        values.put(name, value);
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        JObject jObject = new JObject();
        for (String key : values.keySet()) {
            jObject.set(new JString(key), values.get(key).eval(context));
        }
        return jObject;
    }
}

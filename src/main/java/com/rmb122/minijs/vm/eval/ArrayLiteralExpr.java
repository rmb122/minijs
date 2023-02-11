package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JArray;
import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.ArrayList;

public class ArrayLiteralExpr extends Expr {
    private final ArrayList<Expr> values = new ArrayList<>();

    public void addLiteral(Expr value) {
        values.add(value);
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        JArray jArray = new JArray();
        for (Expr value : values) {
            jArray.push(value.eval(context));
        }
        return jArray;
    }
}

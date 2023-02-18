package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JUndefine;

import java.util.ArrayList;

public class ExprList implements Expr {
    private final ArrayList<Expr> exprs = new ArrayList<>();

    public void addExpr(Expr expr) {
        this.exprs.add(expr);
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        JBaseObject returnValue = JUndefine.UNDEFINE;
        for (Expr expr : exprs) {
            returnValue = expr.eval(context);
        }
        return returnValue;
    }

    public boolean isEmpty() {
        return this.exprs.isEmpty();
    }
}

package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.ArrayList;

public class CallOperator extends LeftHandOperator {
    private final ArrayList<Expr> argExprs;

    public CallOperator(ArrayList<Expr> argExprs) {
        this.argExprs = argExprs;
    }

    public ArrayList<JBaseObject> getArgs(Context context) throws JException {
        ArrayList<JBaseObject> args = new ArrayList<>();
        for (Expr expr : argExprs) {
            args.add(expr.eval(context));
        }
        return args;
    }
}

package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.ArrayList;

public class LeftHandExpr implements Expr {
    private final Expr baseExpr;
    private final ArrayList<LeftHandOperator> operators = new ArrayList<>();

    public LeftHandExpr(Expr baseExpr) {
        this.baseExpr = baseExpr;
    }

    public void addOperator(LeftHandOperator operator) {
        this.operators.add(operator);
    }

    public Expr getBaseExpr() {
        return baseExpr;
    }

    public ArrayList<LeftHandOperator> getOperators() {
        return operators;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        return this.getObject(context, this.operators.size());
    }

    public JBaseObject getObject(Context context, int operatorLength) throws JException {
        JBaseObject thisObject = context.getThis();
        JBaseObject object = baseExpr.eval(context);

        for (LeftHandOperator leftHandOperator : operators.subList(0, operatorLength)) {
            if (leftHandOperator instanceof MemberOperator memberOperator) {
                thisObject = object;
                object = object.get(memberOperator.eval(context).toJString());
            } else if (leftHandOperator instanceof CallOperator callOperator) {
                object = object.call(context, thisObject, callOperator.getArgs(context));
                // call 完之后重置 thisObject a.b()()
                thisObject = context.getThis();
            } else {
                throw new JException("Unexpected operator type");
            }
        }
        return object;
    }
}

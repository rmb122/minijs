package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;

import java.util.List;

public class AssigmentExpr implements Expr {
    private final LeftHandExpr leftHandExpr;
    private final Expr rightHandExpr;

    public AssigmentExpr(LeftHandExpr leftHandExpr, Expr rightHandExpr) {
        this.leftHandExpr = leftHandExpr;
        this.rightHandExpr = rightHandExpr;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        JBaseObject value = rightHandExpr.eval(context);
        List<LeftHandOperator> operators = leftHandExpr.getOperators();

        if (operators.size() == 0 &&
                leftHandExpr.getBaseExpr() instanceof Variable variable &&
                variable.getType() == Variable.Type.IDENTIFIER) {
            // 如果 Operators 为空, 那么 base 必须是个变量
            context.setVariable(variable.getId(), value);
        } else if (operators.size() > 0 && operators.get(operators.size() - 1) instanceof MemberOperator lastMember) {
            // Operators 不为空, 最后一个必须是 MemberOperator
            JBaseObject object = leftHandExpr.getObject(context, operators.size() - 1);
            object.set(lastMember.eval(context).toJString(), value);
        } else {
            throw new JException("Invalid left-hand side in assignment");
        }

        return value;
    }
}

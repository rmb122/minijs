package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JBoolean;
import com.rmb122.minijs.vm.object.JNumber;
import com.rmb122.minijs.vm.object.JObject;

import java.util.ArrayList;
import java.util.List;

public class UnaryExpr implements Expr {
    public enum Op {
        MINUS,
        NEW,
        DELETE,
    }

    private final Expr baseExpr;
    private final Op op;

    public UnaryExpr(Expr baseExpr, Op op) {
        this.baseExpr = baseExpr;
        this.op = op;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        switch (this.op) {
            case NEW -> {
                if (baseExpr instanceof LeftHandExpr baseLeftHandExpr) {
                    List<LeftHandOperator> operators = baseLeftHandExpr.getOperators();
                    // 除了最后一个, 其他 Operator 必须是 member
                    JObject newObject = new JObject();

                    if (operators.size() == 0) {
                        baseLeftHandExpr.eval(context).call(context, newObject, new ArrayList<>());
                        return newObject;
                    }

                    if (operators.subList(0, operators.size() - 1).stream().allMatch(x -> x instanceof MemberOperator)) {
                        if (operators.get(operators.size() - 1) instanceof MemberOperator) {
                            baseLeftHandExpr.eval(context).call(context, newObject, new ArrayList<>());
                        } else if (operators.get(operators.size() - 1) instanceof CallOperator callOperator) {
                            baseLeftHandExpr.getObject(context, operators.size() - 1).call(context, newObject, callOperator.getArgs(context));
                        }
                        return newObject;
                    } else {
                        throw new JException("Multiple new expr call found");
                    }
                } else {
                    throw new JException("Invalid new expr");
                }
            }
            case MINUS -> {
                return new JNumber(-this.baseExpr.eval(context).toJNumber().getValue());
            }
            case DELETE -> {
                if (baseExpr instanceof LeftHandExpr baseLeftHandExpr) {
                    List<LeftHandOperator> operators = baseLeftHandExpr.getOperators();
                    // 除了最后一个, 其他 Operator 必须是 member
                    if (operators.size() == 0 && baseLeftHandExpr.getBaseExpr() instanceof Variable variable && variable.getType() == Variable.Type.IDENTIFIER) {
                        if (context.variableExistsInLocal(variable.getId())) {
                            return JBoolean.FALSE;
                        } else {
                            context.removeGlobalVariable(variable.getId());
                            return JBoolean.TRUE;
                        }
                    } else if (operators.size() > 0 && operators.get(operators.size() - 1) instanceof MemberOperator memberOperator) {
                        baseLeftHandExpr.getObject(context, operators.size() - 1).remove(memberOperator.eval(context).toJString());
                        return JBoolean.TRUE;
                    }
                }

                throw new JException("Invalid delete expr");
            }
            default -> throw new JException("Unexpected unary op");
        }
    }
}

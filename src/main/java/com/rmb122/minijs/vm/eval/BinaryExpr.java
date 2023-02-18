package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JBoolean;
import com.rmb122.minijs.vm.object.JNumber;
import com.rmb122.minijs.vm.object.JString;

public class BinaryExpr implements Expr {
    public enum Op {
        MUL,
        DIV,
        MOD,
        ADD,
        SUB,
        GT,
        GTE,
        LT,
        LTE,
        NE,
        EQ,
        AND,
        OR,
    }

    private final Expr leftExpr;
    private final Expr rightExpr;
    private final Op op;

    public BinaryExpr(Expr leftExpr, Expr rightExpr, Op op) {
        this.leftExpr = leftExpr;
        this.rightExpr = rightExpr;
        this.op = op;
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        // 如果两侧存在一个字符串, 则按照字符串, 否则都按照数字

        JBaseObject leftValue = leftExpr.eval(context);
        JBaseObject rightValue = rightExpr.eval(context);

        if (leftValue instanceof JString || rightValue instanceof JString) {
            String leftString = leftValue.toJString().toString();
            String rightString = rightValue.toJString().toString();

            switch (this.op) {
                case MUL, DIV, MOD, SUB -> {
                    return JNumber.NAN;
                }
                case ADD -> {
                    return new JString(leftString.concat(rightString));
                }
                case GT -> {
                    return JBoolean.valueOf(leftString.compareTo(rightString) > 0);
                }
                case GTE -> {
                    return JBoolean.valueOf(leftString.compareTo(rightString) >= 0);
                }
                case LT -> {
                    return JBoolean.valueOf(leftString.compareTo(rightString) < 0);
                }
                case LTE -> {
                    return JBoolean.valueOf(leftString.compareTo(rightString) <= 0);
                }
                case NE -> {
                    return JBoolean.valueOf(!leftString.equals(rightString));
                }
                case EQ -> {
                    return JBoolean.valueOf(leftString.equals(rightString));
                }
                case AND -> {
                    if (leftString.length() == 0) {
                        return leftValue;
                    } else {
                        return rightValue;
                    }
                }
                case OR -> {
                    if (leftString.length() == 0) {
                        return rightValue;
                    } else {
                        return leftValue;
                    }
                }
                default -> throw new JException("Unexpected binary op");
            }
        } else {
            double leftNumber = leftValue.toJNumber().getValue();
            double rightNumber = rightValue.toJNumber().getValue();

            switch (this.op) {
                case MUL -> {
                    return new JNumber(leftNumber * rightNumber);
                }
                case DIV -> {
                    return new JNumber(leftNumber / rightNumber);
                }
                case MOD -> {
                    return new JNumber(leftNumber % rightNumber);
                }
                case ADD -> {
                    return new JNumber(leftNumber + rightNumber);
                }
                case SUB -> {
                    return new JNumber(leftNumber - rightNumber);
                }
                case GT -> {
                    return JBoolean.valueOf(leftNumber > rightNumber);
                }
                case GTE -> {
                    return JBoolean.valueOf(leftNumber >= rightNumber);
                }
                case LT -> {
                    return JBoolean.valueOf(leftNumber < rightNumber);
                }
                case LTE -> {
                    return JBoolean.valueOf(leftNumber <= rightNumber);
                }
                case NE -> {
                    return JBoolean.valueOf(leftNumber != rightNumber);
                }
                case EQ -> {
                    return JBoolean.valueOf(leftNumber == rightNumber);
                }
                case AND -> {
                    if (leftNumber == 0) {
                        return leftValue;
                    } else {
                        return rightValue;
                    }
                }
                case OR -> {
                    if (leftNumber == 0) {
                        return rightValue;
                    } else {
                        return leftValue;
                    }
                }
                default -> throw new JException("Unexpected binary op");
            }
        }
    }
}

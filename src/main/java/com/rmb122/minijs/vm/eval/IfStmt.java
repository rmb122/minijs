package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBoolean;

public class IfStmt implements Stmt {
    private final ExprList conditionExpr;
    private final StmtList trueStmts;
    private final StmtList falseStmts;

    public IfStmt(ExprList conditionExpr, StmtList trueStmts, StmtList falseStmts) {
        this.conditionExpr = conditionExpr;
        this.trueStmts = trueStmts;
        this.falseStmts = falseStmts;
    }

    public StmtList getBodyStmts(Context context) throws JException {
        if (JBoolean.valueOf(this.conditionExpr.eval(context).toJNumber()).toPrimitive()) {
            return trueStmts;
        } else {
            return falseStmts;
        }
    }
}

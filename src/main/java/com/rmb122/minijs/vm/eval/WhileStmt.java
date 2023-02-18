package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBoolean;

public class WhileStmt implements Stmt, LoopStmt {
    private final ExprList conditionExpr;
    private final StmtList bodyStmts;

    public WhileStmt(ExprList conditionExpr, StmtList bodyStmts) {
        this.conditionExpr = conditionExpr;
        this.bodyStmts = bodyStmts;
    }

    @Override
    public void init(Context context) throws JException {
    }

    @Override
    public boolean stopped(Context context) throws JException {
        return !JBoolean.valueOf(this.conditionExpr.eval(context).toJNumber()).toPrimitive();
    }

    @Override
    public void restart(Context context) throws JException {
    }

    public StmtList getBodyStmts() {
        return this.bodyStmts;
    }
}

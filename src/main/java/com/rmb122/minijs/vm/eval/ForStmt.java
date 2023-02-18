package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBoolean;

public class ForStmt implements Stmt, LoopStmt {
    public enum Type {
        EXPR,
        STMT,
    }

    private final Type type;

    private ExprList initExpr;
    private VariableDefinitionStmt initStmt;

    private final ExprList conditionExpr;
    private final ExprList incrementExpr;
    private final StmtList bodyStmts;

    public ForStmt(ExprList initExpr, ExprList conditionExpr, ExprList incrementExpr, StmtList bodyStmts) {
        this.type = Type.EXPR;

        this.initExpr = initExpr;
        this.conditionExpr = conditionExpr;
        this.incrementExpr = incrementExpr;
        this.bodyStmts = bodyStmts;
    }

    public ForStmt(VariableDefinitionStmt initStmt, ExprList conditionExpr, ExprList incrementExpr, StmtList bodyStmts) {
        this.type = Type.STMT;

        this.initStmt = initStmt;
        this.conditionExpr = conditionExpr;
        this.incrementExpr = incrementExpr;
        this.bodyStmts = bodyStmts;
    }

    @Override
    public void init(Context context) throws JException {
        switch (this.type) {
            case EXPR -> this.initExpr.eval(context);
            case STMT -> this.initStmt.eval(context);
        }
    }

    @Override
    public boolean stopped(Context context) throws JException {
        // for (;;) 死循环
        if (conditionExpr.isEmpty()) {
            return false;
        } else {
            return !JBoolean.valueOf(this.conditionExpr.eval(context).toJNumber()).toPrimitive();
        }
    }

    @Override
    public void restart(Context context) throws JException {
        this.incrementExpr.eval(context);
    }

    @Override
    public StmtList getBodyStmts() {
        return bodyStmts;
    }
}

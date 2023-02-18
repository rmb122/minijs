package com.rmb122.minijs.vm.eval;

public class ReturnStmt implements Stmt {
    private final Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }
}

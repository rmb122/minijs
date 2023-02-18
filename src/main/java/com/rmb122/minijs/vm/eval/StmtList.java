package com.rmb122.minijs.vm.eval;

import java.util.ArrayList;

public class StmtList implements Stmt {
    private final ArrayList<Stmt> stmts = new ArrayList<>();

    public void addStmt(Stmt stmt) {
        this.stmts.add(stmt);
    }

    public Stmt getStmt(int index) {
        return this.stmts.get(index);
    }

    public int size() {
        return stmts.size();
    }
}

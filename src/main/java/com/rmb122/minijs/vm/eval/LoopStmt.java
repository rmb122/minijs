package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;

public interface LoopStmt {
    void init(Context context) throws JException;

    boolean stopped(Context context) throws JException;

    void restart(Context context) throws JException;

    StmtList getBodyStmts();
}

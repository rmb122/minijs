package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.eval.Stmt;

import java.util.ArrayList;
import java.util.List;

public class JFunction extends JObject {
    List<String> args = new ArrayList<>();
    List<Stmt> stmts = new ArrayList<>();

    @Override
    public JBaseObject call(Context context, List<JBaseObject> args) throws JException {
        return super.call(context, args);
    }
}

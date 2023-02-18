package com.rmb122.minijs.vm.object;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.VM;
import com.rmb122.minijs.vm.eval.StmtList;

import java.util.List;

public class JFunction extends JObject {
    String name;
    List<String> params;
    StmtList stmts;

    public JFunction(String name, List<String> params, StmtList stmts) {
        this.name = name;
        this.params = params;
        this.stmts = stmts;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public JBaseObject call(Context context, JBaseObject thisObject, List<JBaseObject> args) throws JException {
        context.pushFrame(thisObject);
        for (int i = 0; i < params.size(); i++) {
            if (i < args.size()) {
                context.setLocalVariable(params.get(i), args.get(i));
            } else {
                break;
            }
        }

        JBaseObject returnObject = VM.run(context, this.stmts);
        context.popFrame();
        return returnObject;
    }

    @Override
    public JString toJString() {
        return new JString(String.format("function %s(%s) { [function body] }", this.name, String.join(", ", this.params)));
    }
}

package com.rmb122.minijs.vm;

import com.rmb122.minijs.vm.eval.Stmt;
import com.rmb122.minijs.vm.eval.StmtList;
import com.rmb122.minijs.vm.object.*;

import java.util.ArrayList;

public class Program {
    private final ArrayList<JFunction> functions = new ArrayList<>();
    private final StmtList stmtList = new StmtList();

    public JBaseObject eval() throws JException {
        Context context = new Context();
        initNativeFunction(context);

        for (JFunction function : functions) {
            context.setGlobalVariable(function.getName(), function);
        }

        return VM.run(context, stmtList);
    }

    private static void initNativeFunction(Context context) throws JException {
        JObject console = new JObject();
        console.set(new JString("log"), new JNativeFunction("log", (callCtx, args) -> {
            for (JBaseObject arg : args) {
                System.out.print(arg.toJString().toString());
                System.out.print(" ");
            }
            System.out.print("\n");
            return JUndefine.UNDEFINE;
        }));

        context.setGlobalVariable("console", console);
    }

    public void addFunction(JFunction function) {
        this.functions.add(function);
    }

    public void addStmt(Stmt stmt) {
        this.stmtList.addStmt(stmt);
    }
}

package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JBaseObject;
import com.rmb122.minijs.vm.object.JUndefine;

import java.util.ArrayList;

public class VariableDefinitionStmt extends Stmt {
    public static class VariableDefinition {
        String name;
        Expr value;

        public VariableDefinition(String name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    ArrayList<VariableDefinition> variableDefinitionList = new ArrayList<>();

    public void addVariableDefinition(String name, Expr expr) {
        variableDefinitionList.add(new VariableDefinition(name, expr));
    }

    @Override
    public JBaseObject eval(Context context) throws JException {
        for (VariableDefinition variableDefinition : this.variableDefinitionList) {
            String name = variableDefinition.name;
            Expr value = variableDefinition.value;

            if (variableDefinition.value == null) {
                // var a
                if (!context.currFrame.variables.containsKey(name)) {
                    context.currFrame.variables.put(name, JUndefine.UNDEFINE);
                }
            } else {
                // var a = 1
                context.currFrame.variables.put(name, value.eval(context));
            }
        }

        return JUndefine.UNDEFINE;
    }
}

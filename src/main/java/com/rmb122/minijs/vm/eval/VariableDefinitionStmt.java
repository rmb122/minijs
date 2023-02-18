package com.rmb122.minijs.vm.eval;

import com.rmb122.minijs.vm.Context;
import com.rmb122.minijs.vm.JException;
import com.rmb122.minijs.vm.object.JUndefine;

import java.util.ArrayList;

public class VariableDefinitionStmt implements Stmt {
    public static class VariableDefinition {
        private final String name;
        private final Expr value;

        public VariableDefinition(String name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    ArrayList<VariableDefinition> variableDefinitionList = new ArrayList<>();

    public void addVariableDefinition(VariableDefinition variableDefinition) {
        variableDefinitionList.add(variableDefinition);
    }

    public void eval(Context context) throws JException {
        for (VariableDefinition variableDefinition : this.variableDefinitionList) {
            String name = variableDefinition.name;
            Expr value = variableDefinition.value;

            if (variableDefinition.value == null) {
                // var a
                if (!context.variableExistsInLocal(name)) {
                    context.setLocalVariable(name, JUndefine.UNDEFINE);
                }
            } else {
                // var a = 1
                context.setLocalVariable(name, value.eval(context));
            }
        }
    }
}

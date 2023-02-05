package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.TokenValue;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AST {
    Symbol symbol;
    Production production;
    TokenValue tokenValue;
    List<AST> children;

    public AST(Symbol symbol, Production production, TokenValue tokenValue, AST... children) {
        this.symbol = symbol;
        this.production = production;
        this.tokenValue = tokenValue;
        this.children = List.of(children);
    }

    public AST(Symbol symbol, Production production, TokenValue tokenValue, List<AST> children) {
        this.symbol = symbol;
        this.production = production;
        this.tokenValue = tokenValue;
        this.children = children;
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph AST {\n");
        HashMap<AST, Integer> astIDMap = new HashMap<>();

        int uniqueID = 1;
        HashSet<AST> workList = new HashSet<>();
        workList.add(this);
        astIDMap.put(this, uniqueID++);

        while (!workList.isEmpty()) {
            AST currAST = workList.iterator().next();
            workList.remove(currAST);

            Integer currASTID = astIDMap.get(currAST);
            String label;
            if (currAST.symbol.terminating) {
                label = currAST.tokenValue.getValue();
            } else {
                label = currAST.production.toString();
            }
            sb.append(String.format("\t%d [label=\"%s\\n%s\"]\n", currASTID, currAST.symbol, StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(label))));
            for (AST child : currAST.children) {
                Integer childID = uniqueID++;
                astIDMap.put(child, childID);
                sb.append("\t").append(currASTID).append(" -> ").append(childID).append("\n");
            }

            workList.addAll(currAST.children);
        }

        sb.append("}");
        return sb.toString();
    }
}

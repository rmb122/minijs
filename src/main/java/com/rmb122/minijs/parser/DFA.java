package com.rmb122.minijs.parser;

import java.util.HashSet;
import java.util.stream.Collectors;

public class DFA {
    private int uniqueID = 0;
    DFAState startState;

    public DFAState newState() {
        this.uniqueID++;
        return new DFAState(this.uniqueID);
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph DFA {\n");

        HashSet<DFAState> visitedState = new HashSet<>();
        HashSet<DFAState> workList = new HashSet<>();
        workList.add(this.startState);

        while (!workList.isEmpty()) {
            DFAState currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);
            String label = String.valueOf(currState.id);

            if (currState == this.startState) {
                label += "\\n[START]";
            }
            label += String.format("\\n%s", currState.lr1Productions.stream().map(LR1Production::toString).collect(Collectors.joining("\\n")));
            sb.append("\t").append(currState.id).append(String.format(" [label=\"%s\"];\n", label));

            for (Symbol symbol : currState.getEdgeKeys()) {
                DFAState targetState = currState.getNextState(symbol);
                if (!visitedState.contains(targetState)) {
                    workList.add(targetState);
                }
                sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"];\n", symbol.toString()));
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

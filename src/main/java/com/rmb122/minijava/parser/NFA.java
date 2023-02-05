package com.rmb122.minijava.parser;

import java.util.HashMap;
import java.util.HashSet;

public class NFA {
    private int uniqueID = 0;
    State startState;

    public static class State {
        int id;
        boolean start;
        boolean end;
        // 规约产生式
        Production reduceProduction;
        private final HashMap<Symbol, HashSet<State>> edges = new HashMap<>();
        private HashSet<State> closureSet;

        public State(int id) {
            this.id = id;
        }

        public void addEdge(Symbol symbol, State state) {
            edges.computeIfAbsent(symbol, k -> new HashSet<>()).add(state);
        }

        public void setEdge(Symbol symbol, HashSet<State> states) {
            this.edges.put(symbol, states);
        }

        public HashSet<State> getEdge(Symbol symbol) {
            return edges.getOrDefault(symbol, new HashSet<>());
        }

        public boolean edgeExists(Symbol symbol) {
            return edges.containsKey(symbol);
        }

        public HashSet<State> getClosureSet() {
            if (this.closureSet != null) {
                return this.closureSet;
            }

            HashSet<State> closureSet = new HashSet<>();
            HashSet<State> workList = new HashSet<>();
            workList.add(this);

            while (!workList.isEmpty()) {
                State currState = workList.iterator().next();
                workList.remove(currState);
                closureSet.add(currState);

                HashSet<State> nextStates = currState.getEdge(Symbol.EMPTY_SYMBOL);
                for (State nextState : nextStates) {
                    if (!closureSet.contains(nextState)) {
                        workList.add(nextState);
                        closureSet.add(nextState);
                    }
                }
            }

            this.closureSet = closureSet;
            return closureSet;
        }

        @Override
        public int hashCode() {
            return this.id;
        }
    }

    public State newState() {
        this.uniqueID++;
        return new State(this.uniqueID);
    }

    public HashSet<Symbol> getPossibleSymbols(HashSet<State> states) {
        HashSet<Symbol> symbols = new HashSet<>();
        for (State state : states) {
            for (State equalState : state.getClosureSet()) {
                symbols.addAll(equalState.edges.keySet());
            }
        }

        // 去掉空边
        symbols.remove(Symbol.EMPTY_SYMBOL);
        return symbols;
    }

    public HashSet<State> getNextStates(HashSet<State> states, Symbol symbol) {
        HashSet<State> nextStates = new HashSet<>();
        for (State state : states) {
            for (State equalState : state.getClosureSet()) {
                nextStates.addAll(equalState.getEdge(symbol));
            }
        }
        return nextStates;
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph NFA {\n");

        HashSet<State> visitedState = new HashSet<>();
        HashSet<State> workList = new HashSet<>();
        workList.add(this.startState);

        while (!workList.isEmpty()) {
            State currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            String label = String.valueOf(currState.id);
            if (currState.start) {
                label += "\\n[START]";
            }
            if (currState.end) {
                label += String.format("\\n%s", currState.reduceProduction.toString());
            }
            sb.append("\t").append(currState.id).append(String.format(" [label=\"%s\"];\n", label));

            for (Symbol symbol : currState.edges.keySet()) {
                HashSet<State> targetStates = currState.edges.get(symbol);
                for (State targetState : targetStates) {
                    if (!visitedState.contains(targetState)) {
                        workList.add(targetState);
                    }
                    sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"];\n", symbol.toString()));
                }
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

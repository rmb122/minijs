package com.rmb122.minijava.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class DFA {
    private int uniqueID = 0;
    State startState;

    public static class State {
        int id;
        boolean start;
        boolean end;
        HashSet<ProductionLookahead> productionLookaheads = new HashSet<>();
        private final HashMap<Symbol, State> edges = new HashMap<>();
        public HashSet<NFA.State> nfaStates;

        public State(int id, HashSet<NFA.State> nfaStates) {
            this.id = id;

            this.nfaStates = nfaStates;
            for (NFA.State s : nfaStates) {
                for (NFA.State equalState : s.getClosureSet()) {
                    // 注意需要调用 copy(), 否则会共享 lookaheadSymbol
                    this.productionLookaheads.addAll(equalState.productionLookaheads.stream().map(ProductionLookahead::copy).toList());
                    if (equalState.end) {
                        this.end = true;
                    }
                }
            }
        }

        public void addEdge(Symbol symbol, State state) throws RuntimeException {
            if (edges.get(symbol) != null) {
                throw new RuntimeException("when transforming NFA to DFA, duplicate edge found");
            }
            edges.put(symbol, state);
        }

        public State getEdge(Symbol s) {
            return edges.get(s);
        }

        public HashMap<Symbol, State> getEdges() {
            return this.edges;
        }

        @Override
        public int hashCode() {
            return this.id;
        }
    }

    public State newState(HashSet<NFA.State> nfaStates) {
        this.uniqueID++;
        return new State(this.uniqueID, nfaStates);
    }

    public static DFA fromNFA(NFA nfa) {
        DFA dfa = new DFA();

        HashMap<HashSet<NFA.State>, State> existedStates = new HashMap<>();
        HashSet<State> visitedState = new HashSet<>();
        HashSet<State> workList = new HashSet<>();

        HashSet<NFA.State> nfaStartClosureSet = nfa.startState.getClosureSet();
        dfa.startState = dfa.newState(nfaStartClosureSet);
        dfa.startState.start = true;

        State currState = dfa.startState;
        existedStates.put(nfaStartClosureSet, currState);
        workList.add(currState);

        while (!workList.isEmpty()) {
            currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            HashSet<Symbol> edgeRunes = nfa.getPossibleSymbols(currState.nfaStates);
            for (Symbol symbol : edgeRunes) {
                HashSet<NFA.State> nextStates = nfa.getNextStates(currState.nfaStates, symbol);
                State dfaState = existedStates.get(nextStates);
                if (dfaState == null) {
                    dfaState = dfa.newState(nextStates);
                    existedStates.put(nextStates, dfaState);
                }

                if (!visitedState.contains(dfaState)) {
                    workList.add(dfaState);
                }
                currState.addEdge(symbol, dfaState);
            }
        }

        return dfa;
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph DFA {\n");

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
            label += String.format("\\n%s", currState.productionLookaheads.stream().map(ProductionLookahead::toString).collect(Collectors.joining("\\n")));
            sb.append("\t").append(currState.id).append(String.format(" [label=\"%s\"];\n", label));

            for (Symbol symbol : currState.edges.keySet()) {
                State targetState = currState.edges.get(symbol);
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

package com.rmb122.minijava.regexp;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.HashSet;

public class DFA<C> {
    private int uniqueID = 0;
    State<C> startState;

    public static class State<C> {
        int id;
        boolean start;
        boolean end;
        HashSet<C> containerSet = new HashSet<>();
        private final HashMap<Rune, State<C>> edges = new HashMap<>();
        public HashSet<NFA.State<C>> nfaStates;

        public State(int id, HashSet<NFA.State<C>> nfaStates) {
            this.id = id;

            this.nfaStates = nfaStates;
            for (NFA.State<C> s : nfaStates) {
                for (NFA.State<C> equalState : s.getClosureSet()) {
                    if (equalState.end) {
                        this.end = true;
                        this.containerSet.add(equalState.container);
                    }
                }
            }
        }

        public void addEdge(Rune r, State<C> s) throws RegexpCompileError {
            if (edges.get(r) != null) {
                throw new RegexpCompileError("when transforming NFA to DFA, duplicate edge found");
            }
            edges.put(r, s);
        }

        public State<C> getEdge(Rune r) {
            return edges.get(r);
        }

        @Override
        public int hashCode() {
            return this.id;
        }
    }

    public State<C> newState(HashSet<NFA.State<C>> nfaStates) {
        this.uniqueID++;
        return new State<C>(this.uniqueID, nfaStates);
    }

    public static <C> DFA<C> fromNFA(NFA<C> nfa) throws RegexpCompileError {
        DFA<C> dfa = new DFA<C>();

        HashMap<HashSet<NFA.State<C>>, State<C>> existedStates = new HashMap<>();
        HashSet<State<C>> visitedState = new HashSet<>();
        HashSet<State<C>> workList = new HashSet<>();

        HashSet<NFA.State<C>> nfaStartClosureSet = nfa.startState.getClosureSet();
        dfa.startState = dfa.newState(nfaStartClosureSet);
        dfa.startState.start = true;

        State<C> currState = dfa.startState;
        existedStates.put(nfaStartClosureSet, currState);
        workList.add(currState);

        while (!workList.isEmpty()) {
            currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            HashSet<Rune> edgeRunes = nfa.getPossibleRunes(currState.nfaStates);
            for (Rune r : edgeRunes) {
                HashSet<NFA.State<C>> nextStates = nfa.getNextStates(currState.nfaStates, r);
                State<C> dfaState = existedStates.get(nextStates);
                if (dfaState == null) {
                    dfaState = dfa.newState(nextStates);
                    existedStates.put(nextStates, dfaState);
                }

                if (!visitedState.contains(dfaState)) {
                    workList.add(dfaState);
                }
                currState.addEdge(r, dfaState);
            }
        }

        return dfa;
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph DFA {\n");

        HashSet<State<C>> visitedState = new HashSet<>();
        HashSet<State<C>> workList = new HashSet<>();
        workList.add(this.startState);

        while (!workList.isEmpty()) {
            State<C> currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);
            String label = String.valueOf(currState.id);

            if (currState.start) {
                label += "\\n[START]";
            }
            if (currState.end) {
                label += "\\n[END]";
            }
            sb.append("\t").append(currState.id).append(String.format(" [label=\"%s\"];\n", label));

            for (Rune r : currState.edges.keySet()) {
                State<C> targetState = currState.edges.get(r);
                if (!visitedState.contains(targetState)) {
                    workList.add(targetState);
                }
                sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"];\n", StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(r.toString()))));
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

package com.rmb122.regexp;

import java.util.HashMap;
import java.util.HashSet;

public class DFA<C> {
    private int uniqueID = 0;

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

    State<C> startState;

    public State<C> newState(HashSet<NFA.State<C>> nfaStates) {
        this.uniqueID++;
        return new State<C>(this.uniqueID, nfaStates);
    }

    public static <C> DFA<C> fromNFA(NFA.State<C> startState) throws RegexpCompileError {
        DFA<C> dfa = new DFA<C>();

        HashMap<HashSet<NFA.State<C>>, State<C>> existedStates = new HashMap<>();
        HashSet<State<C>> visitedState = new HashSet<>();
        HashSet<State<C>> workList = new HashSet<>();

        HashSet<NFA.State<C>> nfaStartClosureSet = startState.getClosureSet();
        dfa.startState = dfa.newState(nfaStartClosureSet);
        dfa.startState.start = true;

        State<C> currState = dfa.startState;
        existedStates.put(nfaStartClosureSet, currState);
        workList.add(currState);

        while (!workList.isEmpty()) {
            currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            HashSet<Rune> edgeRunes = NFA.getPossibleRunes(currState.nfaStates);
            for (Rune r : edgeRunes) {
                HashSet<NFA.State<C>> nextStates = NFA.getNextStates(currState.nfaStates, r);
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

    public static <C> String generateDOTFile(State<C> startState) {
        StringBuilder sb = new StringBuilder("digraph DFA {\n");

        HashSet<State<C>> visitedState = new HashSet<>();
        HashSet<State<C>> workList = new HashSet<>();
        workList.add(startState);

        while (!workList.isEmpty()) {
            State<C> currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            for (Rune r : currState.edges.keySet()) {
                State<C> targetState = currState.edges.get(r);
                if (!visitedState.contains(targetState)) {
                    workList.add(targetState);
                }
                sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"]\n", r.toString().replace("\"", "\\\"")));
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

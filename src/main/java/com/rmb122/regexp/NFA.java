package com.rmb122.regexp;

import java.util.HashMap;
import java.util.HashSet;

public class NFA<C> {
    private int uniqueID = 0;

    public static class State<C> {
        int id;
        boolean start;
        boolean end;
        C container;
        private final HashMap<Rune, HashSet<State<C>>> edges = new HashMap<>();
        private HashSet<State<C>> closureSet;

        public State(int id) {
            this.id = id;
        }

        public void addEdge(Rune r, State<C> s) {
            HashSet<State<C>> l = edges.computeIfAbsent(r, k -> new HashSet<>());
            l.add(s);
        }

        public HashSet<State<C>> getEdge(Rune r) {
            return edges.getOrDefault(r, new HashSet<>());
        }

        public HashSet<State<C>> getClosureSet() {
            if (this.closureSet != null) {
                return this.closureSet;
            }

            HashSet<State<C>> closureSet = new HashSet<>();
            HashSet<State<C>> workList = new HashSet<>();
            workList.add(this);

            while (!workList.isEmpty()) {
                State<C> currState = workList.iterator().next();
                workList.remove(currState);
                closureSet.add(currState);

                HashSet<State<C>> nextStates = currState.getEdge(Rune.EMPTY_CHAR);
                for (State<C> nextState : nextStates) {
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

    public State<C> newState() {
        this.uniqueID++;
        return new State<C>(this.uniqueID);
    }

    public static <C> HashSet<Rune> getPossibleRunes(HashSet<State<C>> states) {
        HashSet<Rune> runes = new HashSet<>();
        for (State<C> state : states) {
            for (State<C> equalState : state.getClosureSet()) {
                runes.addAll(equalState.edges.keySet());
            }
        }

        // 去掉空边
        runes.remove(Rune.EMPTY_CHAR);
        return runes;
    }

    public static <C> HashSet<State<C>> getNextStates(HashSet<State<C>> states, Rune r) {
        // 非 . 字符可以走 . 的边
        // 这样在 DFA 匹配时, 检测字符是否存在, 不存在再走 ., 如果存在则不走 .

        HashSet<State<C>> nextStates = new HashSet<>();
        for (State<C> state : states) {
            for (State<C> equalState : state.getClosureSet()) {
                nextStates.addAll(equalState.getEdge(r));
                if (r != Rune.ANY_CHAR) {
                    nextStates.addAll(equalState.getEdge(Rune.ANY_CHAR));
                }
            }
        }
        return nextStates;
    }

    public static <C> String generateDOTFile(State<C> startState) {
        StringBuilder sb = new StringBuilder("digraph NFA {\n");

        HashSet<State<C>> visitedState = new HashSet<>();
        HashSet<State<C>> workList = new HashSet<>();
        workList.add(startState);

        while (!workList.isEmpty()) {
            State<C> currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            String label = String.valueOf(currState.id);
            if (currState.start) {
                label += "\\n[start]";
            }
            if (currState.end) {
                label += "\\n[end]";
            }
            sb.append("\t").append(currState.id).append(String.format(" [label=\"%s\"];\n", label));

            for (Rune r : currState.edges.keySet()) {
                for (State<C> targetState : currState.edges.get(r)) {
                    if (!visitedState.contains(targetState)) {
                        workList.add(targetState);
                    }
                    sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"];\n", r.toString().replace("\"", "\\\"")));
                }
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

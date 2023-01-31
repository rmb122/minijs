package com.rmb122.regexp;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.HashSet;

public class NFA<C> {
    private int uniqueID = 0;
    State<C> startState;
    public HashSet<State<C>> STOP_STATE = new HashSet<>();

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

        public void setEdge(Rune r, HashSet<State<C>> s) {
            this.edges.put(r, s);
        }

        public HashSet<State<C>> getEdge(Rune r) {
            return edges.getOrDefault(r, new HashSet<>());
        }

        public boolean edgeExists(Rune r) {
            return edges.containsKey(r);
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

    public HashSet<Rune> getPossibleRunes(HashSet<State<C>> states) {
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

    public HashSet<State<C>> getNextStates(HashSet<State<C>> states, Rune r) {
        // 非 . 字符可以走 . 的边
        // 这样在 DFA 匹配时, 检测字符是否存在, 不存在再走 ., 如果存在则不走 .
        // 但是如果非 . 字符的目标是 STOP_STATE, 停止匹配 ANY_CHAR

        HashSet<State<C>> nextStates = new HashSet<>();
        for (State<C> state : states) {
            for (State<C> equalState : state.getClosureSet()) {
                HashSet<State<C>> targetStates = equalState.getEdge(r);
                if (targetStates != this.STOP_STATE) {
                    nextStates.addAll(targetStates);
                    if (r != Rune.ANY_CHAR) {
                        nextStates.addAll(equalState.getEdge(Rune.ANY_CHAR));
                    }
                }
            }
        }
        return nextStates;
    }

    public String generateDOTFile() {
        StringBuilder sb = new StringBuilder("digraph NFA {\n");
        sb.append("\tSTOP [label=\"[STOP]\"]\n");

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
                HashSet<State<C>> targetStates = currState.edges.get(r);
                if (targetStates != this.STOP_STATE) {
                    for (State<C> targetState : targetStates) {
                        if (!visitedState.contains(targetState)) {
                            workList.add(targetState);
                        }
                        sb.append("\t").append(currState.id).append(" -> ").append(targetState.id).append(String.format(" [label=\"%s\"];\n", StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(r.toString()))));
                    }
                } else {
                    sb.append("\t").append(currState.id).append(" -> ").append("STOP").append(String.format(" [label=\"%s\"];\n", StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(r.toString()))));
                }
            }
        }

        sb.append("}");
        return sb.toString();
    }
}

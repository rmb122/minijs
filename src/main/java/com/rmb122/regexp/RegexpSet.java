package com.rmb122.regexp;

import java.util.HashSet;
import java.util.List;

public class RegexpSet<C> {
    NFA<C> nfa = new NFA<>();
    DFA<C> dfa;
    NFA.State<C> nfaStartState = this.nfa.newState();
    HashSet<RegexpOption> options;

    public RegexpSet(RegexpOption... options) {
        this.options = new HashSet<>(List.of(options));
    }

    public void addPattern(String pattern, C c) throws RegexpCompileError {
        RegexpCompiler.NFAStatePair<C> statePair = RegexpCompiler.compileWithNFA(this.nfa, pattern, this.options.toArray(new RegexpOption[]{}));
        statePair.endState.end = true;
        statePair.endState.container = c;

        nfaStartState.addEdge(Rune.EMPTY_CHAR, statePair.startState);
    }

    public void compile() throws RegexpCompileError {
        this.dfa = DFA.fromNFA(nfaStartState);
    }

    public RegexpMatchResult<C> matchNext(String input, int startPos) {
        DFA.State<C> currState = this.dfa.startState;
        RegexpMatchResult<C> matchResult = null;
        int currPos = startPos;

        for (; currPos < input.length(); currPos++) {
            char currChar = input.charAt(currPos);
            DFA.State<C> nextState = currState.getEdge(new Rune(currChar));
            if (nextState == null) {
                if (this.options.contains(RegexpOption.DOT_ALL) || currChar != '\n') {
                    nextState = currState.getEdge(Rune.ANY_CHAR);
                }
            }

            if (nextState == null) {
                break;
            } else {
                currState = nextState;
            }
        }

        if (currState.end) {
            matchResult = new RegexpMatchResult<>(currState.containerSet, currPos - startPos);
        }
        return matchResult;
    }
}

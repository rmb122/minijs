package com.rmb122.minijs.regexp;

import java.util.HashSet;
import java.util.List;

public class RegexpSet<C> {
    NFA<C> nfa = new NFA<>();
    DFA<C> dfa;
    HashSet<RegexpOption> options;

    public RegexpSet(RegexpOption... options) {
        this.options = new HashSet<>(List.of(options));
        this.nfa.startState = this.nfa.newState();
    }

    public void addPattern(String pattern, C c) throws RegexpCompileError {
        RegexpCompiler.NFAStatePair<C> statePair = RegexpCompiler.compileWithNFA(this.nfa, pattern, this.options.toArray(new RegexpOption[]{}));
        statePair.endState.end = true;
        statePair.endState.container = c;

        this.nfa.startState.addEdge(Rune.EMPTY_CHAR, statePair.startState);
    }

    public void compile() throws RegexpCompileError {
        this.dfa = DFA.fromNFA(this.nfa);
        if (this.options.contains(RegexpOption.DEBUG)) {
            System.out.println(this.nfa.generateDOTFile());
            System.out.println(this.dfa.generateDOTFile());
        }
    }

    public RegexpMatchResult<C> matchNext(String input, int startPos) {
        DFA.State<C> currState = this.dfa.startState;
        RegexpMatchResult<C> matchResult = null;
        int currPos = startPos;

        for (; currPos < input.length(); currPos++) {
            char currChar = input.charAt(currPos);
            DFA.State<C> nextState = currState.getEdge(new Rune(currChar));
            if (nextState == null) {
                nextState = currState.getEdge(Rune.ANY_CHAR);
            }

            // 下一个状态为 null, 或者下一个状态不是 end 且出边为空 (NFA 转成 DFA 后的 STOP_STATE)
            if (nextState == null || (!nextState.end && nextState.getEdgeKeys().size() == 0)) {
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

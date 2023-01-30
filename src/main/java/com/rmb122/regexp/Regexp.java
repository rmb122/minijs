package com.rmb122.regexp;

import java.util.Arrays;
import java.util.HashSet;

public class Regexp<C> {
    DFA<C> dfa;
    String pattern;
    HashSet<RegexpOption> options;

    private Regexp() {
    }

    public static Regexp<Void> compile(String pattern, RegexpOption... options) throws RegexpCompileError {
        Regexp<Void> regexp = new Regexp<>();
        regexp.options = new HashSet<>(Arrays.asList(options));
        regexp.pattern = pattern;

        RegexpCompiler.NFAStatePair<Void> statePair = RegexpCompiler.compile(pattern, options);
        statePair.endState.end = true;

        regexp.dfa = DFA.fromNFA(statePair.startState);

        if (regexp.options.contains(RegexpOption.DEBUG)) {
            System.out.println(DFA.generateDOTFile(regexp.dfa.startState));
        }
        return regexp;
    }

    public boolean matchString(String s) {
        DFA.State<C> currState = this.dfa.startState;

        for (int i = 0; i < s.length(); i++) {
            char currChar = s.charAt(i);
            DFA.State<C> nextState = currState.getEdge(new Rune(currChar));
            if (nextState == null) {
                if (this.options.contains(RegexpOption.DOT_ALL) || currChar != '\n') {
                    nextState = currState.getEdge(Rune.ANY_CHAR);
                }
            }
            currState = nextState;
            if (currState == null) {
                return false;
            }
        }

        return currState.end;
    }

    public static void main(String[] args) throws Exception {
        Regexp<Void> r = Regexp.compile(".*aax?aaaz[A-Za![]cd+zxc.*asd", RegexpOption.DEBUG);
        System.out.println(r.matchString("aaxaaaz!cddddddzxc !! asd"));
    }
}

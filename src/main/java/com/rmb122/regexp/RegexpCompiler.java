package com.rmb122.regexp;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RegexpCompiler<C> {
    NFA<C> nfa;
    String pattern;
    HashSet<RegexpOption> options;

    // ] 在正常情况下被认为是普通字符
    private final static HashSet<Character> specialChars = new HashSet<>(List.of('*', '+', '?', '|', '(', ')', '['));
    private final static HashSet<Character> hexDigest = new HashSet<>(List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'));

    public static class NFAStatePair<C> {
        NFA.State<C> startState;
        NFA.State<C> endState;
    }

    private static class RuneRange {
        Rune startRune;
        Rune endRune;

        public RuneRange(Rune startRune, Rune endRune) {
            this.startRune = startRune;
            this.endRune = endRune;
        }
    }

    public static <C> NFAStatePair<C> compile(String pattern, RegexpOption... options) throws RegexpCompileError {
        RegexpCompiler<C> compiler = new RegexpCompiler<>();
        compiler.options = new HashSet<>(Arrays.asList(options));
        compiler.pattern = pattern;

        /*
            expr ::= term ("|" term)*
            term ::= (factor | factor （"*" | "+" | "?"）)*
            factor ::= rune | "(" expr ")" | "[" group "]"
            group ::= (rune | rune "-" rune)*
         */
        compiler.nfa = new NFA<>();

        StringCharacterIterator iterator = new StringCharacterIterator(compiler.pattern);
        NFAStatePair<C> statePair = compiler.parseExpr(iterator);
        if (iterator.getIndex() != iterator.getEndIndex()) {
            throw new RegexpCompileError("extra data at regexp tail found");
        }

        if (compiler.options.contains(RegexpOption.DEBUG)) {
            System.out.println(NFA.generateDOTFile(statePair.startState));
        }
        return statePair;
    }

    public static <C> NFAStatePair<C> compileWithNFA(NFA<C> nfa, String pattern, RegexpOption... options) throws RegexpCompileError {
        RegexpCompiler<C> compiler = new RegexpCompiler<>();
        compiler.options = new HashSet<>(Arrays.asList(options));
        compiler.pattern = pattern;

        compiler.nfa = nfa;

        StringCharacterIterator iterator = new StringCharacterIterator(compiler.pattern);
        NFAStatePair<C> statePair = compiler.parseExpr(iterator);
        if (iterator.getIndex() != iterator.getEndIndex()) {
            throw new RegexpCompileError("extra data at regexp tail found");
        }

        if (compiler.options.contains(RegexpOption.DEBUG)) {
            System.out.println(NFA.generateDOTFile(statePair.startState));
        }
        return statePair;
    }

    private NFAStatePair<C> parseExpr(StringCharacterIterator iterator) throws RegexpCompileError {
        NFAStatePair<C> statePair = new NFAStatePair<C>();
        statePair.startState = this.nfa.newState();
        statePair.endState = this.nfa.newState();

        ArrayList<NFAStatePair<C>> termStatePairs = new ArrayList<>();

        while (iterator.current() != CharacterIterator.DONE) {
            termStatePairs.add(this.parseTerm(iterator));

            char currChar = iterator.current();
            if (currChar == '|') {
                iterator.next(); // 吃掉 '|'
            } else {
                break;
            }
        }

        for (NFAStatePair<C> pair : termStatePairs) {
            statePair.startState.addEdge(Rune.EMPTY_CHAR, pair.startState);
            pair.endState.addEdge(Rune.EMPTY_CHAR, statePair.endState);
        }

        return statePair;
    }

    private NFAStatePair<C> parseTerm(StringCharacterIterator iterator) throws RegexpCompileError {
        NFAStatePair<C> statePair = new NFAStatePair<C>();
        statePair.startState = this.nfa.newState();
        NFA.State<C> currState = statePair.startState;

        while (iterator.current() != CharacterIterator.DONE) {
            NFAStatePair<C> factorStatePair = this.parseFactor(iterator);
            char currChar = iterator.current();

            if (currChar == '*') {
                iterator.next(); // 吃掉 '*'
                NFA.State<C> nextState = this.nfa.newState();
                if (factorStatePair.startState == factorStatePair.endState) {
                    throw new RegexpCompileError(String.format("invalid '*' at regexp pos %d", iterator.getIndex()));
                }

                factorStatePair.startState.addEdge(Rune.EMPTY_CHAR, nextState);
                factorStatePair.endState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);
                currState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);

                currState = nextState;
            } else if (currChar == '?') {
                iterator.next(); // 吃掉 '?'
                NFA.State<C> nextState = this.nfa.newState();
                if (factorStatePair.startState == factorStatePair.endState) {
                    throw new RegexpCompileError(String.format("invalid '?' at regexp pos %d", iterator.getIndex()));
                }

                factorStatePair.startState.addEdge(Rune.EMPTY_CHAR, nextState);
                factorStatePair.endState.addEdge(Rune.EMPTY_CHAR, nextState);
                currState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);

                currState = nextState;
            } else if (currChar == '+') {
                iterator.next(); // 吃掉 '+'
                NFA.State<C> nextState = this.nfa.newState();
                if (factorStatePair.startState == factorStatePair.endState) {
                    throw new RegexpCompileError(String.format("invalid '+' at regexp pos %d", iterator.getIndex()));
                }

                factorStatePair.endState.addEdge(Rune.EMPTY_CHAR, nextState);
                factorStatePair.endState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);
                currState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);

                currState = nextState;
            } else {
                NFA.State<C> nextState = this.nfa.newState();

                currState.addEdge(Rune.EMPTY_CHAR, factorStatePair.startState);
                factorStatePair.endState.addEdge(Rune.EMPTY_CHAR, nextState);

                currState = nextState;

                if (specialChars.contains(currChar) && currChar != '(' && currChar != '[') {
                    // 当前字符是除了 (, [ 以外的特殊字符, 连接状态后 break
                    break;
                }
            }
        }

        statePair.endState = currState;
        return statePair;
    }

    private Rune readRune(StringCharacterIterator iterator, boolean dotAsAny) throws RegexpCompileError {
        char currChar = iterator.current();
        iterator.next(); // 吃掉当前匹配的字符

        if (currChar == '.' && dotAsAny) {
            return Rune.ANY_CHAR;
        } else if (currChar == '\\') {
            currChar = iterator.current();
            if (currChar == CharacterIterator.DONE) {
                throw new RegexpCompileError("invalid '\\' at end of regexp");
            }
            switch (currChar) {
                case 'n' -> currChar = '\n';
                case 'r' -> currChar = '\r';
                case 't' -> currChar = '\t';
                case '0' -> currChar = '\0';
                case 'x' -> {
                    char hex1 = iterator.next();  // x
                    char hex2 = iterator.next();  // hex1

                    if (hex1 == CharacterIterator.DONE || hex2 == CharacterIterator.DONE || !(hexDigest.contains(hex1) && hexDigest.contains(hex2))) {
                        throw new RegexpCompileError(String.format("invalid hex escape at regexp pos %d", iterator.getIndex()));
                    }
                    currChar = (char) Integer.parseInt(String.copyValueOf(new char[]{hex1, hex2}), 16);
                }
            }

            iterator.next();
            return new Rune(currChar);
        } else {
            return new Rune(currChar);
        }
    }

    private NFAStatePair<C> parseFactor(StringCharacterIterator iterator) throws RegexpCompileError {
        NFAStatePair<C> statePair = new NFAStatePair<>();
        statePair.startState = this.nfa.newState();
        NFA.State<C> currState = statePair.startState;

        char currChar = iterator.current();

        if (specialChars.contains(currChar)) {
            if (currChar == '(') {
                iterator.next(); // 吃掉 '('

                NFAStatePair<C> exprStatePair = this.parseExpr(iterator);
                currState.addEdge(Rune.EMPTY_CHAR, exprStatePair.startState);
                currState = exprStatePair.endState;
                if (iterator.current() == ')') {
                    iterator.next(); // 吃掉 ')'
                } else {
                    throw new RegexpCompileError(String.format("expecting ')' at regexp pos %d", iterator.getIndex()));
                }
            } else if (currChar == '[') {
                iterator.next(); // 吃掉 '['
                NFAStatePair<C> groupStatePair = this.parseGroup(iterator);

                currState.addEdge(Rune.EMPTY_CHAR, groupStatePair.startState);
                currState = groupStatePair.endState;
                if (iterator.current() == ']') {
                    iterator.next(); // 吃掉 ']'
                } else {
                    throw new RegexpCompileError(String.format("expecting ']' at regexp pos %d", iterator.getIndex()));
                }
            }
        } else {
            Rune rune = this.readRune(iterator, true);
            NFA.State<C> nextState = this.nfa.newState();
            currState.addEdge(rune, nextState);
            currState = nextState;
        }

        statePair.endState = currState;
        return statePair;
    }

    private NFAStatePair<C> parseGroup(StringCharacterIterator iterator) throws RegexpCompileError {
        NFAStatePair<C> statePair = new NFAStatePair<>();
        statePair.startState = this.nfa.newState();
        statePair.endState = this.nfa.newState();

        HashSet<Rune> runeSet = new HashSet<>();

        while (iterator.current() != CharacterIterator.DONE) {
            char currChar = iterator.current();
            if (currChar == ']') {
                break;
            }

            Rune currRune = this.readRune(iterator, false);
            if (iterator.current() == '-') {
                if (iterator.next() == ']') {
                    throw new RegexpCompileError(String.format("invalid ']' at regexp pos %d", iterator.getIndex()));
                }

                Rune endRune = this.readRune(iterator, false);
                for (int i = currRune.c; i < endRune.c; i++) {
                    runeSet.add(new Rune((char) i));
                }
            } else {
                runeSet.add(currRune);
            }
        }

        for (Rune rune : runeSet) {
            statePair.startState.addEdge(rune, statePair.endState);
        }

        return statePair;
    }
}

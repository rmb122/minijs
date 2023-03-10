package com.rmb122.minijs.regexp;

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

    public static <C> NFA<C> compile(String pattern, RegexpOption... options) throws RegexpCompileError {
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

        statePair.endState.end = true;
        compiler.nfa.startState = statePair.startState;
        return compiler.nfa;
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

                // 类似 "asdxx|", | 在最后一个的特殊情况, 匹配空字符串
                if (iterator.current() == CharacterIterator.DONE) {
                    statePair.startState.addEdge(Rune.EMPTY_CHAR, statePair.endState);
                }
            } else {
                break;
            }
        }

        if (termStatePairs.size() == 0) {
            // 正则为空的特殊情况
            statePair.startState.addEdge(Rune.EMPTY_CHAR, statePair.endState);
        } else {
            for (NFAStatePair<C> pair : termStatePairs) {
                statePair.startState.addEdge(Rune.EMPTY_CHAR, pair.startState);
                pair.endState.addEdge(Rune.EMPTY_CHAR, statePair.endState);
            }
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
                // 检测 a** 的情况
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
        // dotAsAny 指示是否在 group [A-z.] 中. 在 group 中 . 不代表 ANY_CHAR
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
                case 'u' -> {
                    char hex1 = iterator.next();  // u
                    char hex2 = iterator.next();  // hex1
                    char hex3 = iterator.next();  // hex2
                    char hex4 = iterator.next();  // hex3

                    if (hex1 == CharacterIterator.DONE || hex2 == CharacterIterator.DONE ||
                            hex3 == CharacterIterator.DONE || hex4 == CharacterIterator.DONE ||
                            !(hexDigest.contains(hex1) && hexDigest.contains(hex2) && hexDigest.contains(hex3) && hexDigest.contains(hex4))
                    ) {
                        throw new RegexpCompileError(String.format("invalid unicode escape at regexp pos %d", iterator.getIndex()));
                    }
                    currChar = (char) Integer.parseInt(String.copyValueOf(new char[]{hex1, hex2, hex3, hex4}), 16);
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
            // 如果是除了 (, [ 之外的特殊符号, 返回一个空的状态
        } else {
            Rune rune = this.readRune(iterator, true);
            if (rune == Rune.ANY_CHAR && !this.options.contains(RegexpOption.DOT_ALL)) {
                // 不是 DOT_ALL, 开一条边 \n 通向 STOP_STATE
                currState.setEdge(new Rune('\n'), this.nfa.STOP_STATE);
            }
            NFA.State<C> nextState = this.nfa.newState();
            currState.addEdge(rune, nextState);
            currState = nextState;
        }

        statePair.endState = currState;
        return statePair;
    }

    private NFAStatePair<C> parseGroup(StringCharacterIterator iterator) throws RegexpCompileError {
        NFAStatePair<C> statePair = new NFAStatePair<>();
        boolean notMatch = false;
        statePair.startState = this.nfa.newState();
        statePair.endState = this.nfa.newState();

        HashSet<Rune> runeSet = new HashSet<>();

        if (iterator.current() == '^') {
            // [^a] 第一个是 ^ 匹配除了集合之外的字符
            notMatch = true;
            iterator.next();
        }

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
                for (int i = currRune.c; i <= endRune.c; i++) {
                    runeSet.add(new Rune((char) i));
                }
            } else {
                runeSet.add(currRune);
            }
        }

        if (!notMatch) {
            // 匹配集合内的字符
            for (Rune rune : runeSet) {
                statePair.startState.addEdge(rune, statePair.endState);
            }
        } else {
            // 匹配集合外的字符
            statePair.startState.addEdge(Rune.ANY_CHAR, statePair.endState);

            for (Rune rune : runeSet) {
                statePair.startState.setEdge(rune, this.nfa.STOP_STATE);
            }
        }
        return statePair;
    }
}

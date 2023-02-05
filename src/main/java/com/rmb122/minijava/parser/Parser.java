package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.Lexer;
import com.rmb122.minijava.lexer.Token;
import com.rmb122.minijava.lexer.TokenValue;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    HashMap<Symbol, Set<Production>> productionMap = new HashMap<>();
    HashSet<Symbol> terminalSymbol = new HashSet<>(List.of(Symbol.EOF_SYMBOL));
    HashSet<Symbol> nonTerminalSymbol = new HashSet<>();

    Symbol startSymbol;
    DFA dfa;

    HashMap<DFA.State, HashMap<Symbol, ParserAction>> actionTable = new HashMap<>();
    HashMap<DFA.State, HashMap<Symbol, DFA.State>> gotoTable = new HashMap<>();

    public void addProduction(Production production) throws ParserError {
        if (production.head.terminating) {
            throw new ParserError("the head of production must not a terminal symbol");
        }

        this.nonTerminalSymbol.add(production.head);

        for (Symbol symbol : production.body) {
            if (symbol.terminating) {
                this.terminalSymbol.add(symbol);
            } else {
                this.nonTerminalSymbol.add(symbol);
            }
        }

        productionMap.computeIfAbsent(production.head, k -> new HashSet<>()).add(production);
    }

    public void setStartSymbol(Symbol symbol) throws ParserError {
        if (startSymbol == null) {
            startSymbol = symbol;

            // 设置增广产生式
            this.addProduction(new Production(Symbol.EXTEND_START_SYMBOL, startSymbol));
        } else {
            throw new ParserError("startSymbol already exists");
        }
    }

    private void addAction(DFA.State state, Symbol symbol, ParserAction action) throws ParserError {
        HashMap<Symbol, ParserAction> actionLine = this.actionTable.computeIfAbsent(state, k -> new HashMap<>());
        if (actionLine.containsKey(symbol)) {
            throw new ParserError("not a valid action table / syntax, duplicate entry found");
        } else {
            actionLine.put(symbol, action);
        }
    }

    private ParserAction getAction(DFA.State state, Symbol symbol) {
        return this.actionTable.get(state).get(symbol);
    }

    private void addGoto(DFA.State state, Symbol symbol, DFA.State targetState) throws ParserError {
        HashMap<Symbol, DFA.State> gotoLine = this.gotoTable.computeIfAbsent(state, k -> new HashMap<>());
        if (gotoLine.containsKey(symbol)) {
            throw new ParserError("not a valid goto table / syntax, duplicate entry found");
        } else {
            gotoLine.put(symbol, targetState);
        }
    }

    private DFA.State getGoto(DFA.State state, Symbol symbol) {
        return this.gotoTable.get(state).get(symbol);
    }

    public void compile() throws ParserError {
        if (this.startSymbol == null) {
            throw new ParserError("startSymbol need to be set");
        }

        NFA nfa = new NFA();

        HashMap<Symbol, NFA.State> symbolStartState = new HashMap<>();
        for (Symbol headSymbol : productionMap.keySet()) {
            NFA.State state = nfa.newState();
            symbolStartState.put(headSymbol, state);

            if (headSymbol.equals(Symbol.EXTEND_START_SYMBOL)) {
                nfa.startState = state;
                state.start = true;
            }
        }

        for (Symbol headSymbol : productionMap.keySet()) {
            for (Production production : productionMap.get(headSymbol)) {
                NFA.State currState = symbolStartState.get(headSymbol);

                if (production.body.size() != 0) {
                    for (int i = 0; i < production.body.size(); i++) {
                        Symbol currSymbol = production.body.get(i);
                        NFA.State newState = nfa.newState();
                        currState.addEdge(currSymbol, newState);

                        if (!currSymbol.terminating) {
                            if (!symbolStartState.containsKey(currSymbol)) {
                                throw new ParserError(String.format("symbol %s should have at least one production", currSymbol));
                            }

                            currState.addEdge(Symbol.EMPTY_SYMBOL, symbolStartState.get(currSymbol));
                        }

                        if (i == production.body.size() - 1) {
                            newState.end = true;
                            newState.reduceProduction = production;
                        }

                        currState = newState;
                    }
                } else {
                    // 空产生式, 代表这个符号可以为空
                    currState.addEdge(Symbol.EMPTY_SYMBOL, currState);
                    currState.end = true;
                    currState.reduceProduction = production;
                }
            }
        }

        this.dfa = DFA.fromNFA(nfa);

        System.out.println(nfa.generateDOTFile());
        System.out.println(dfa.generateDOTFile());

        HashSet<DFA.State> workList = new HashSet<>();
        HashSet<DFA.State> visitedState = new HashSet<>();
        workList.add(this.dfa.startState);

        while (!workList.isEmpty()) {
            DFA.State currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            // LR0 实现
            if (currState.end) {
                if (currState.reduceProductions.size() == 1 && currState.reduceProductions.iterator().next().head == Symbol.EXTEND_START_SYMBOL) {
                    // 增广产生式收到 EOF, 产生 ACC
                    this.addAction(currState, Symbol.EOF_SYMBOL, ParserAction.ACCEPT_PARSER_ACTION);
                } else {
                    for (Symbol symbol : terminalSymbol) {
                        if (currState.reduceProductions.size() != 1 || currState.getEdges().size() != 0) {
                            // LR0 无法解决规约-移进冲突
                            throw new ParserError("not a lr0 syntax");
                        }
                        this.addAction(currState, symbol, new ParserAction(currState.reduceProductions.iterator().next()));
                    }
                }
            } else {
                for (Symbol symbol : currState.getEdges().keySet()) {
                    if (symbol.terminating) {
                        this.addAction(currState, symbol, new ParserAction(currState.getEdge(symbol)));
                    } else {
                        this.addGoto(currState, symbol, currState.getEdge(symbol));
                    }
                }
            }

            for (DFA.State nextState : currState.getEdges().values()) {
                if (!visitedState.contains(nextState)) {
                    workList.add(nextState);
                }
            }
        }
    }

    public void generateParserTableCsv() {
        // action 表
        ArrayList<String> tableHead = new ArrayList<>(List.of("State"));
        HashMap<Symbol, Integer> symbolColumnMap = new HashMap<>();

        List<Symbol> terminalSymbolList = this.terminalSymbol.stream().sorted(Comparator.comparing(Symbol::toString)).toList();
        for (int i = 0; i < terminalSymbolList.size(); i++) {
            tableHead.add(terminalSymbolList.get(i).toString());
            // 表头 State, 需要 + 1
            symbolColumnMap.put(terminalSymbolList.get(i), i + 1);
        }

        ArrayList<ArrayList<String>> tableContent = new ArrayList<>();

        for (DFA.State state : this.actionTable.keySet().stream().sorted(Comparator.comparingInt(s -> s.id)).toList()) {
            ArrayList<String> currTableContentLine = new ArrayList<>(Collections.nCopies(tableHead.size(), ""));
            currTableContentLine.set(0, String.valueOf(state.id));

            HashMap<Symbol, ParserAction> actionLine = this.actionTable.get(state);
            for (Symbol symbol : actionLine.keySet()) {
                currTableContentLine.set(symbolColumnMap.get(symbol), actionLine.get(symbol).toString());
            }

            tableContent.add(currTableContentLine);
        }

        System.out.println(tableHead.stream().map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(",")));

        tableContent.forEach(line -> {
            System.out.println(line.stream().map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(",")));
        });

        System.out.println();
        // goto 表
        tableHead = new ArrayList<>(List.of("State"));
        symbolColumnMap = new HashMap<>();

        List<Symbol> nonTerminalSymbolList = this.nonTerminalSymbol.stream().sorted(Comparator.comparing(Symbol::toString)).toList();
        for (int i = 0; i < nonTerminalSymbolList.size(); i++) {
            tableHead.add(nonTerminalSymbolList.get(i).toString());
            // 表头 State, 需要 + 1
            symbolColumnMap.put(nonTerminalSymbolList.get(i), i + 1);
        }

        tableContent = new ArrayList<>();

        for (DFA.State state : this.gotoTable.keySet().stream().sorted(Comparator.comparingInt(s -> s.id)).toList()) {
            ArrayList<String> currTableContentLine = new ArrayList<>(Collections.nCopies(tableHead.size(), ""));
            currTableContentLine.set(0, String.valueOf(state.id));

            HashMap<Symbol, DFA.State> gotoLine = this.gotoTable.get(state);
            for (Symbol symbol : gotoLine.keySet()) {
                currTableContentLine.set(symbolColumnMap.get(symbol), String.valueOf(gotoLine.get(symbol).id));
            }

            tableContent.add(currTableContentLine);
        }

        System.out.println(tableHead.stream().map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(",")));

        tableContent.forEach(line -> {
            System.out.println(line.stream().map(StringEscapeUtils::escapeCsv).collect(Collectors.joining(",")));
        });
    }

    public AST parse(List<TokenValue> tokenValues) throws ParserError {
        Stack<ParserState> stateStack = new Stack<>();
        // 初始栈状态
        stateStack.push(new ParserState(this.dfa.startState, Symbol.EOF_SYMBOL, null, null));

        int tokenValueIdx = 0;
        while (tokenValueIdx <= tokenValues.size()) {
            TokenValue currTokenValue = null;
            Symbol currSymbol;

            if (tokenValueIdx < tokenValues.size()) {
                currTokenValue = tokenValues.get(tokenValueIdx);
                currSymbol = currTokenValue.getToken().asSymbol();
            } else {
                // 最后一个是 EOF
                currSymbol = Symbol.EOF_SYMBOL;
            }

            ParserState currState = stateStack.peek();
            ParserAction action = this.getAction(currState.dfaState, currSymbol);

            if (action == null) {
                if (currTokenValue != null) {
                    throw new ParserError(String.format("invalid syntax at line %d, col %d", currTokenValue.getLineNum(), currTokenValue.getColNum()));
                } else {
                    throw new ParserError("unexpected EOF");
                }
            }

            switch (action.type) {
                case SHIFT -> {
                    stateStack.push(new ParserState(action.gotoState, currSymbol, currTokenValue, new AST(currSymbol, null, currTokenValue)));
                    tokenValueIdx++;
                }
                case ACCEPT -> {
                    if (tokenValueIdx != tokenValues.size()) {
                        throw new ParserError("unexpected accept");
                    } else {
                        // 如果 tokenValueIdx == tokenValues.size(), 会正常退出循环
                        tokenValueIdx++;
                    }
                }
                case REDUCE -> {
                    List<Symbol> productionBody = action.reduceProduction.body;
                    List<AST> children = new ArrayList<>(Collections.nCopies(productionBody.size(), null));

                    for (int i = productionBody.size() - 1; i >= 0; i--) {
                        ParserState popState = stateStack.pop();
                        if (!popState.symbol.equals(productionBody.get(i))) {
                            throw new ParserError("pop state not match the reduce production");
                        }
                        children.set(i, popState.ast);
                    }

                    stateStack.push(
                            new ParserState(
                                    this.getGoto(stateStack.peek().dfaState, action.reduceProduction.head),
                                    action.reduceProduction.head,
                                    null,
                                    new AST(action.reduceProduction.head, action.reduceProduction, null, children)
                            )
                    );
                }
            }
        }

        return stateStack.peek().ast;
    }

    private static void testLR0() throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Token a = new Token("a");
        Token c = new Token("c");
        Token e = new Token("e");
        Token b = new Token("b");
        Token d = new Token("d");

        lexer.addToken("a", a);
        lexer.addToken("c", c);
        lexer.addToken("e", e);
        lexer.addToken("b", b);
        lexer.addToken("d", d);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol A = new Symbol("A");
        Symbol B = new Symbol("B");

        parser.addProduction(new Production(S, a.asSymbol(), A, c.asSymbol(), B, e.asSymbol()));
        parser.addProduction(new Production(A, b.asSymbol()));
        parser.addProduction(new Production(A, A, b.asSymbol()));
        parser.addProduction(new Production(B, d.asSymbol()));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();
        AST result = parser.parse(lexer.scan("abbcde"));
        System.out.println(result);
    }

    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        /*
            NUMBER := [0-9]+

            expr := expr '+' term | term
            term := term '*' factor | factor
            factor := NUMBER | '(' expr ')'
         */

        /*
        Token number = new Token("NUMBER", 0);
        Token plus = new Token("PLUS", 0);
        Token multi = new Token("MULTI", 0);
        Token lpar = new Token("LPAR", 0);
        Token rpar = new Token("RPAR", 0);
        Token blank = new Token("BLANK", 0);

        lexer.addToken("[0-9]+", number);
        lexer.addToken("\\+", plus);
        lexer.addToken("\\*", multi);
        lexer.addToken("\\(", lpar);
        lexer.addToken("\\)", rpar);
        lexer.addToken("[\n\r\t ]+", blank, true);

        lexer.compile();

        Symbol expr = new Symbol("expr");
        Symbol term = new Symbol("term");
        Symbol factor = new Symbol("factor");

        parser.addProduction(new Production(expr, expr, plus.asSymbol(), term));
        parser.addProduction(new Production(expr, term));

        parser.addProduction(new Production(term, term, multi.asSymbol(), factor));
        parser.addProduction(new Production(term, factor));

        parser.addProduction(new Production(factor, number.asSymbol()));
        parser.addProduction(new Production(factor, lpar.asSymbol(), expr, rpar.asSymbol()));

        parser.setStartSymbol(expr);
        parser.compile();
        parser.parse(lexer.scan("1+1"));
         */

        Token a = new Token("a");
        Token b = new Token("b");

        lexer.addToken("a", a);
        lexer.addToken("b", b);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol A = new Symbol("A");
        Symbol B = new Symbol("B");

        parser.addProduction(new Production(S, A, B));
        parser.addProduction(new Production(A, a.asSymbol(), B, a.asSymbol()));
        parser.addProduction(new Production(A));
        parser.addProduction(new Production(B, b.asSymbol(), A, b.asSymbol()));
        parser.addProduction(new Production(B));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();
        AST result = parser.parse(lexer.scan("baab"));
        System.out.println(result);
    }
}

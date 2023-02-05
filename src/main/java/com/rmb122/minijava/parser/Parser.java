package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.Lexer;
import com.rmb122.minijava.lexer.Token;
import com.rmb122.minijava.lexer.TokenValue;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    HashMap<Symbol, Set<Production>> productionMap = new HashMap<>();
    HashSet<Symbol> terminalSymbolSet = new HashSet<>(List.of(Symbol.EOF_SYMBOL));
    HashSet<Symbol> nonTerminalSymbolSet = new HashSet<>();

    Symbol startSymbol;
    DFA dfa;

    HashMap<Symbol, HashSet<Symbol>> firstSet = new HashMap<>();
    HashMap<Symbol, HashSet<Symbol>> followSet = new HashMap<>();

    HashMap<DFA.State, HashMap<Symbol, ParserAction>> actionTable = new HashMap<>();
    HashMap<DFA.State, HashMap<Symbol, DFA.State>> gotoTable = new HashMap<>();

    public void addProduction(Production production) throws ParserError {
        if (production.head.terminating) {
            throw new ParserError("the head of production must not a terminal symbol");
        }

        this.nonTerminalSymbolSet.add(production.head);

        for (Symbol symbol : production.body) {
            if (symbol.terminating) {
                this.terminalSymbolSet.add(symbol);
            } else {
                this.nonTerminalSymbolSet.add(symbol);
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
            throw new ParserError("not a valid LALR(1) syntax, duplicate action table entry found");
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
            throw new ParserError("not a valid LALR(1) syntax, duplicate goto table entry found");
        } else {
            gotoLine.put(symbol, targetState);
        }
    }

    private DFA.State getGoto(DFA.State state, Symbol symbol) {
        return this.gotoTable.get(state).get(symbol);
    }

    private void firstSetAdd(Symbol symbol, Symbol firstSymbol) {
        this.firstSet.computeIfAbsent(symbol, k -> new HashSet<>()).add(firstSymbol);
    }

    private void firstSetAddAll(Symbol symbol, Collection<Symbol> firstSymbols) {
        this.firstSet.computeIfAbsent(symbol, k -> new HashSet<>()).addAll(firstSymbols);
    }

    private HashSet<Symbol> firstSetGet(Symbol symbol) {
        return this.firstSet.computeIfAbsent(symbol, k -> new HashSet<>());
    }

    private void followSetAdd(Symbol symbol, Symbol followSymbol) {
        this.followSet.computeIfAbsent(symbol, k -> new HashSet<>()).add(followSymbol);
    }

    private void followSetAddAll(Symbol symbol, Collection<Symbol> followSymbol) {
        this.followSet.computeIfAbsent(symbol, k -> new HashSet<>()).addAll(followSymbol);
    }

    private HashSet<Symbol> followSetGet(Symbol symbol) {
        return this.followSet.computeIfAbsent(symbol, k -> new HashSet<>());
    }

    private void calcSymbolFirstSet() {
        this.firstSetAdd(Symbol.EMPTY_SYMBOL, Symbol.EMPTY_SYMBOL);

        for (Symbol terminalSymbol : terminalSymbolSet) {
            this.firstSetAdd(terminalSymbol, terminalSymbol);
        }

        for (Symbol headSymbol : productionMap.keySet()) {
            for (Production production : productionMap.get(headSymbol)) {
                if (production.body.size() == 0) {
                    // 为空
                    this.firstSetAdd(headSymbol, Symbol.EMPTY_SYMBOL);
                } else if (production.body.get(0).terminating) {
                    // 第一个是终结符
                    this.firstSetAdd(headSymbol, production.body.get(0));
                }
            }
        }

        boolean changed;
        do {
            changed = false;

            for (Symbol headSymbol : productionMap.keySet()) {
                for (Production production : productionMap.get(headSymbol)) {
                    if (production.body.size() > 0 && !production.body.get(0).terminating) {
                        HashSet<Symbol> oldFirstSet = new HashSet<>(this.firstSetGet(headSymbol));

                        int i = 0;
                        while (i < production.body.size()) {
                            Symbol currSymbol = production.body.get(i);

                            // 去掉 empty, 只有产生式中的符号全部为空时候, 才认为这个符号可以为空
                            HashSet<Symbol> appendFirstSet = new HashSet<>(this.firstSetGet(currSymbol));
                            appendFirstSet.remove(Symbol.EMPTY_SYMBOL);

                            this.firstSetAddAll(headSymbol, appendFirstSet);
                            if (!this.firstSetGet(currSymbol).contains(Symbol.EMPTY_SYMBOL)) {
                                break;
                            } else {
                                i++;
                            }
                        }

                        if (i == production.body.size()) {
                            this.firstSetAdd(headSymbol, Symbol.EMPTY_SYMBOL);
                        }

                        if (!this.firstSetGet(headSymbol).equals(oldFirstSet)) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }

    private void calcSymbolFollowSet() {
        this.followSetAdd(Symbol.EXTEND_START_SYMBOL, Symbol.EOF_SYMBOL);

        boolean changed;
        do {
            changed = false;

            for (Symbol nonTerminalSymbol : nonTerminalSymbolSet) {
                HashSet<Symbol> oldFollowSet = new HashSet<>(this.followSetGet(nonTerminalSymbol));

                for (Symbol headSymbol : productionMap.keySet()) {
                    for (Production production : productionMap.get(headSymbol)) {
                        for (int i = 0; i < production.body.size(); i++) {
                            // 找到当前 nonTerminalSymbol
                            if (production.body.get(i).equals(nonTerminalSymbol)) {
                                // 从后一个符号开始取
                                int j = i + 1;
                                while (j < production.body.size()) {
                                    // 不是最后一个, 取后一个符号的 first 集
                                    HashSet<Symbol> appendFollowSet = new HashSet<>(this.firstSetGet(production.body.get(j)));
                                    appendFollowSet.remove(Symbol.EMPTY_SYMBOL);

                                    this.followSetAddAll(nonTerminalSymbol, appendFollowSet);

                                    // 如果后一个符号可以为空, 继续求下一个符号, 否则 break
                                    if (!this.firstSetGet(production.body.get(j)).contains(Symbol.EMPTY_SYMBOL)) {
                                        break;
                                    } else {
                                        j++;
                                    }
                                }

                                if (j == production.body.size()) {
                                    // 遍历结束后到达最后, 并上 headSymbol 的 followSet
                                    this.followSetAddAll(nonTerminalSymbol, this.followSetGet(headSymbol));
                                }
                            }
                        }
                    }
                }

                if (!this.followSetGet(nonTerminalSymbol).equals(oldFollowSet)) {
                    changed = true;
                }
            }
        } while (changed);
    }

    private void calcProductionLookahead() {
        // startState 上肯定有增广产生式子, 给它设置 Lookahead 到 EOF_SYMBOL
        DFA.State startState = this.dfa.startState;
        for (ProductionLookahead productionLookahead : startState.productionLookaheads) {
            if (productionLookahead.production.head.equals(Symbol.EXTEND_START_SYMBOL)) {
                productionLookahead.lookaheadSymbols.add(Symbol.EOF_SYMBOL);
            }
        }

        boolean changed;
        do {
            changed = false;

            HashSet<DFA.State> workList = new HashSet<>();
            HashSet<DFA.State> visitedStates = new HashSet<>();
            workList.add(startState);

            while (!workList.isEmpty()) {
                DFA.State currState = workList.iterator().next();
                workList.remove(currState);
                visitedStates.add(currState);

                HashMap<Symbol, HashSet<ProductionLookahead>> currStateLookaheadsMap = new HashMap<>();
                for (ProductionLookahead productionLookahead : currState.productionLookaheads) {
                    currStateLookaheadsMap.computeIfAbsent(productionLookahead.production.head, k -> new HashSet<>()).add(productionLookahead);
                }

                for (ProductionLookahead productionLookahead : currState.productionLookaheads) {
                    // closure
                    if (productionLookahead.index < productionLookahead.production.body.size()) {
                        Symbol currSymbol = productionLookahead.production.body.get(productionLookahead.index);
                        // 当前位置在一个非终结符前面, 取这个非终结符为 currSymbol
                        if (!currSymbol.terminating) {
                            // 找到这个非终结符对应的产生式, 且产生式在开始位置
                            Set<ProductionLookahead> waitingProductionLookaheads = currStateLookaheadsMap.computeIfAbsent(currSymbol, k -> new HashSet<>());
                            waitingProductionLookaheads = ProductionLookahead.findStartProduction(waitingProductionLookaheads);

                            // 需要增加的 lookahead = FIRST(后面的符号 + 当前产生式的 lookahead)
                            Set<Symbol> newLookaheads = new HashSet<>();
                            int i = productionLookahead.index + 1;

                            while (i < productionLookahead.production.body.size()) {
                                Set<Symbol> currFirstSet = this.firstSetGet(productionLookahead.production.body.get(i));
                                newLookaheads.addAll(currFirstSet);

                                // 存在 EMPTY_SYMBOL, 继续找下一个, 不存在就直接 break
                                if (currFirstSet.contains(Symbol.EMPTY_SYMBOL)) {
                                    i++;
                                } else {
                                    break;
                                }
                            }

                            // 去掉空符号
                            newLookaheads.remove(Symbol.EMPTY_SYMBOL);
                            // 到了最后一个, 再加上当前产生式的 lookahead
                            if (i == productionLookahead.production.body.size()) {
                                newLookaheads.addAll(productionLookahead.lookaheadSymbols);
                            }

                            for (ProductionLookahead waitingProductionLookahead : waitingProductionLookaheads) {
                                for (Symbol newLookahead : newLookaheads) {
                                    if (!waitingProductionLookahead.lookaheadSymbols.contains(newLookahead)) {
                                        waitingProductionLookahead.lookaheadSymbols.add(newLookahead);
                                        changed = true;
                                    }
                                }
                            }
                        }

                        // goto
                        // 当前产生式的移位后产生式子继承此产生式的 lookahead
                        DFA.State nextState = currState.getEdge(currSymbol);
                        Set<ProductionLookahead> waitingProductionLookaheads = ProductionLookahead.findProduction(nextState.productionLookaheads, productionLookahead.production, productionLookahead.index + 1);
                        for (ProductionLookahead waitingProductionLookahead : waitingProductionLookaheads) {
                            for (Symbol newLookahead : productionLookahead.lookaheadSymbols) {
                                if (!waitingProductionLookahead.lookaheadSymbols.contains(newLookahead)) {
                                    waitingProductionLookahead.lookaheadSymbols.add(newLookahead);
                                    changed = true;
                                }
                            }
                        }
                    }
                }

                for (DFA.State nextState : currState.getEdges().values()) {
                    if (!visitedStates.contains(nextState)) {
                        workList.add(nextState);
                    }
                }
            }
        } while (changed);
    }

    public void compile() throws ParserError {
        if (this.startSymbol == null) {
            throw new ParserError("startSymbol need to be set");
        }

        this.calcSymbolFirstSet();
        this.calcSymbolFollowSet();

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
                currState.productionLookaheads.add(new ProductionLookahead(production, 0));

                if (production.body.size() != 0) {
                    for (int i = 0; i < production.body.size(); i++) {
                        Symbol currSymbol = production.body.get(i);
                        NFA.State newState = nfa.newState();
                        newState.productionLookaheads.add(new ProductionLookahead(production, i + 1));
                        currState.addEdge(currSymbol, newState);

                        if (!currSymbol.terminating) {
                            if (!symbolStartState.containsKey(currSymbol)) {
                                throw new ParserError(String.format("symbol %s should have at least one production", currSymbol));
                            }

                            currState.addEdge(Symbol.EMPTY_SYMBOL, symbolStartState.get(currSymbol));
                        }

                        if (i == production.body.size() - 1) {
                            newState.end = true;
                        }

                        currState = newState;
                    }
                } else {
                    // 空产生式, 代表这个符号可以为空
                    currState.addEdge(Symbol.EMPTY_SYMBOL, currState);
                    currState.end = true;
                    currState.productionLookaheads.add(new ProductionLookahead(production, 0));
                }
            }
        }

        this.dfa = DFA.fromNFA(nfa);
        this.calcProductionLookahead();

        System.out.println(nfa.generateDOTFile());
        System.out.println(dfa.generateDOTFile());

        HashSet<DFA.State> workList = new HashSet<>();
        HashSet<DFA.State> visitedState = new HashSet<>();
        workList.add(this.dfa.startState);


        while (!workList.isEmpty()) {
            DFA.State currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            // LALR(1) 实现
            if (currState.end) {
                Set<ProductionLookahead> finishedProduction = ProductionLookahead.findFinishedProduction(currState.productionLookaheads);

                for (ProductionLookahead productionLookahead : finishedProduction) {
                    if (productionLookahead.production.head == Symbol.EXTEND_START_SYMBOL) {
                        // 增广产生式收到 EOF, 产生 ACC
                        this.addAction(currState, Symbol.EOF_SYMBOL, ParserAction.PARSER_ACTION_ACCEPT);
                    } else {
                        // 其余正常产生式
                        // LALR(1), 只对产生式 lookahead 集中的终结符产生规约
                        for (Symbol symbol : productionLookahead.lookaheadSymbols) {
                            this.addAction(currState, symbol, new ParserAction(productionLookahead.production));
                        }
                    }
                }
            }

            for (Symbol symbol : currState.getEdges().keySet()) {
                if (symbol.terminating) {
                    this.addAction(currState, symbol, new ParserAction(currState.getEdge(symbol)));
                } else {
                    this.addGoto(currState, symbol, currState.getEdge(symbol));
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

        List<Symbol> terminalSymbolList = this.terminalSymbolSet.stream().sorted(Comparator.comparing(Symbol::toString)).toList();
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

        List<Symbol> nonTerminalSymbolList = this.nonTerminalSymbolSet.stream().sorted(Comparator.comparing(Symbol::toString)).toList();
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

    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Token a = new Token("a");
        Token b = new Token("b");

        lexer.addToken("a", a);
        lexer.addToken("b", b);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol B = new Symbol("B");

        parser.addProduction(new Production(S, B, B));
        parser.addProduction(new Production(B, a.asSymbol(), B));
        parser.addProduction(new Production(B, b.asSymbol()));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();
        AST result = parser.parse(lexer.scan("abab"));
        System.out.println(result.generateDOTFile());
    }
}

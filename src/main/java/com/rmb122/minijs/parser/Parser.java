package com.rmb122.minijs.parser;

import com.rmb122.minijs.lexer.Lexer;
import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.lexer.TokenValue;
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

    HashMap<DFAState, HashMap<Symbol, ParserAction>> actionTable = new HashMap<>();
    HashMap<DFAState, HashMap<Symbol, DFAState>> gotoTable = new HashMap<>();

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

    public void addProduction(Symbolize head, Symbolize... body) throws ParserError {
        this.addProduction(new Production(head, body));
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

    private void addAction(DFAState state, Symbol symbol, ParserAction action) throws ParserError {
        HashMap<Symbol, ParserAction> actionLine = this.actionTable.computeIfAbsent(state, k -> new HashMap<>());
        if (actionLine.containsKey(symbol)) {
            throw new ParserError("not a valid syntax, duplicate action table entry found");
        } else {
            actionLine.put(symbol, action);
        }
    }

    private ParserAction getAction(DFAState state, Symbol symbol) {
        return this.actionTable.get(state).get(symbol);
    }

    private void addGoto(DFAState state, Symbol symbol, DFAState targetState) throws ParserError {
        HashMap<Symbol, DFAState> gotoLine = this.gotoTable.computeIfAbsent(state, k -> new HashMap<>());
        if (gotoLine.containsKey(symbol)) {
            throw new ParserError("not a valid syntax, duplicate goto table entry found");
        } else {
            gotoLine.put(symbol, targetState);
        }
    }

    private DFAState getGoto(DFAState state, Symbol symbol) {
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

    private static class DFAConnectNotify {
        Symbol symbol;
        DFAState dfaState;

        public DFAConnectNotify(Symbol symbol, DFAState dfaState) {
            this.symbol = symbol;
            this.dfaState = dfaState;
        }
    }

    public DFA calcLR1DFA() throws ParserError {
        DFA dfa = new DFA();

        HashMap<HashSet<LR1Production>, DFAState> existedStateMap = new HashMap<>();
        HashMap<DFAState, DFAConnectNotify> notifyMap = new HashMap<>();
        HashSet<DFAState> workList = new HashSet<>();

        dfa.startState = dfa.newState();
        // 放入初始增广式
        dfa.startState.addLR1Production(new LR1Production(this.productionMap.get(Symbol.EXTEND_START_SYMBOL).iterator().next(), 0, new HashSet<>(List.of(Symbol.EOF_SYMBOL))));
        workList.add(dfa.startState);

        while (!workList.isEmpty()) {
            DFAState currState = workList.iterator().next();
            workList.remove(currState);

            // 计算 Closure
            boolean changed;
            do {
                changed = false;

                List<LR1Production> oldLR1Productions = currState.lr1Productions.stream().map(LR1Production::copy).toList();
                for (LR1Production lr1Production : oldLR1Productions) {
                    if (!lr1Production.isFinished() && !lr1Production.currSymbol().terminating) {
                        // 当前符号不是终结符, 寻找对应的产生式
                        Set<Production> newProductions = this.productionMap.get(lr1Production.currSymbol());
                        if (newProductions == null || newProductions.size() == 0) {
                            throw new ParserError(String.format("non-terminal symbol %s should have at least one production", lr1Production.currSymbol()));
                        }
                        HashSet<Symbol> lookaheadSymbols = new HashSet<>();

                        int i = lr1Production.index + 1;
                        while (i < lr1Production.body().size()) {
                            Symbol symbol = lr1Production.getSymbol(i);
                            Set<Symbol> firstSet = this.firstSetGet(symbol);
                            lookaheadSymbols.addAll(firstSet);

                            if (!firstSet.contains(Symbol.EMPTY_SYMBOL)) {
                                break;
                            } else {
                                i++;
                            }
                        }

                        lookaheadSymbols.remove(Symbol.EMPTY_SYMBOL);

                        // 如果后面的全部可能为空, 那么继承当前产生式的 lookaheadSymbols
                        if (i == lr1Production.body().size()) {
                            lookaheadSymbols.addAll(lr1Production.lookaheadSymbols);
                        }

                        for (Production newProduction : newProductions) {
                            currState.addLR1Production(new LR1Production(newProduction, 0, lookaheadSymbols));
                        }
                    }
                }

                if (!oldLR1Productions.equals(currState.lr1Productions)) {
                    changed = true;
                }
            } while (changed);

            DFAConnectNotify notify = notifyMap.get(currState);
            boolean needGoto = true;

            if (notify != null) {
                HashSet<LR1Production> currLR1ProductionSet = new HashSet<>(currState.lr1Productions);
                // 初始节点, notify 可能为 null
                if (existedStateMap.containsKey(currLR1ProductionSet)) {
                    // 如果已经存在, 说明当前节点重复, 将 notify 中的 state 连接到 existedStateMap 中的 state 中
                    notify.dfaState.addNextState(notify.symbol, existedStateMap.get(currLR1ProductionSet));
                    needGoto = false;
                } else {
                    // put 后, 注意不能再修改 currState.lr1Productions 了
                    // 不存在, 那么自己就是第一个, 连上自己
                    existedStateMap.put(currLR1ProductionSet, currState);
                    notify.dfaState.addNextState(notify.symbol, currState);
                }
                notifyMap.remove(currState);
            }

            // 计算 GOTO
            if (!needGoto) {
                // 只有第一次需要 goto
                continue;
            }

            // 下一个符号 -> 对应的 LR1Production
            HashMap<Symbol, HashSet<LR1Production>> nextLR1Productions = new HashMap<>();
            for (LR1Production lr1Production : currState.lr1Productions) {
                // 取当前符号
                if (!lr1Production.isFinished()) {
                    Symbol nextSymbol = lr1Production.currSymbol();
                    nextLR1Productions.computeIfAbsent(nextSymbol, k -> new HashSet<>()).add(lr1Production);
                }
            }

            for (Symbol nextSymbol : nextLR1Productions.keySet()) {
                DFAState nextState = dfa.newState();

                for (LR1Production nextLR1Production : nextLR1Productions.get(nextSymbol)) {
                    // 继承 lookaheadSymbols, index + 1
                    nextState.addLR1Production(new LR1Production(nextLR1Production.production, nextLR1Production.index + 1, nextLR1Production.lookaheadSymbols));
                }

                // 通知下一个 state 构建好时连接当前状态
                notifyMap.put(nextState, new DFAConnectNotify(nextSymbol, currState));
                workList.add(nextState);
            }
        }

        return dfa;
    }

    public void compile() throws ParserError {
        if (this.startSymbol == null) {
            throw new ParserError("startSymbol need to be set");
        }

        this.calcSymbolFirstSet();
        // this.calcSymbolFollowSet();
        this.dfa = calcLR1DFA();
        this.buildParserTable();
    }

    private void buildParserTable() throws ParserError {
        HashSet<DFAState> workList = new HashSet<>();
        HashSet<DFAState> visitedState = new HashSet<>();
        workList.add(this.dfa.startState);

        while (!workList.isEmpty()) {
            DFAState currState = workList.iterator().next();
            workList.remove(currState);
            visitedState.add(currState);

            List<LR1Production> finishedProduction = LR1Production.findFinishedProduction(currState.lr1Productions);

            for (LR1Production lr1Production : finishedProduction) {
                if (lr1Production.production.head == Symbol.EXTEND_START_SYMBOL) {
                    // 增广产生式收到 EOF, 产生 ACC
                    this.addAction(currState, Symbol.EOF_SYMBOL, ParserAction.PARSER_ACTION_ACCEPT);
                } else {
                    // 其余正常产生式
                    // 只对产生式 lookahead 集中的终结符产生规约
                    for (Symbol symbol : lr1Production.lookaheadSymbols) {
                        this.addAction(currState, symbol, new ParserAction(lr1Production.production));
                    }
                }
            }

            for (Symbol symbol : currState.getEdges().keySet()) {
                if (symbol.terminating) {
                    this.addAction(currState, symbol, new ParserAction(currState.getNextState(symbol)));
                } else {
                    this.addGoto(currState, symbol, currState.getNextState(symbol));
                }
            }

            for (DFAState nextState : currState.getEdges().values()) {
                if (!visitedState.contains(nextState)) {
                    workList.add(nextState);
                }
            }
        }
    }

    public void generateParserTableCsv() {
        // dfa
        System.out.println(dfa.generateDOTFile());

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

        for (DFAState state : this.actionTable.keySet().stream().sorted(Comparator.comparingInt(s -> s.id)).toList()) {
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

        for (DFAState state : this.gotoTable.keySet().stream().sorted(Comparator.comparingInt(s -> s.id)).toList()) {
            ArrayList<String> currTableContentLine = new ArrayList<>(Collections.nCopies(tableHead.size(), ""));
            currTableContentLine.set(0, String.valueOf(state.id));

            HashMap<Symbol, DFAState> gotoLine = this.gotoTable.get(state);
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
        Token c = new Token("c");
        Token d = new Token("d");
        Token e = new Token("e");

        lexer.addToken("a", a);
        lexer.addToken("b", b);
        lexer.addToken("c", c);
        lexer.addToken("d", d);
        lexer.addToken("e", e);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol E = new Symbol("E");
        Symbol F = new Symbol("F");

        parser.addProduction(new Production(S, a, E, c));
        parser.addProduction(new Production(S, a, F, d));
        parser.addProduction(new Production(S, b, F, c));
        parser.addProduction(new Production(S, b, E, d));
        parser.addProduction(new Production(E, e));
        parser.addProduction(new Production(F, e));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();

        AST result = parser.parse(lexer.scan("aed"));
        System.out.println(result.generateDOTFile());
    }
}

package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.TokenValue;

public class ParserState {
    public DFAState dfaState;
    public Symbol symbol;
    public TokenValue tokenValue;
    public AST ast;

    public ParserState(DFAState dfaState, Symbol symbol, TokenValue tokenValue, AST ast) {
        this.dfaState = dfaState;
        this.symbol = symbol;
        this.tokenValue = tokenValue;
        this.ast = ast;
    }
}

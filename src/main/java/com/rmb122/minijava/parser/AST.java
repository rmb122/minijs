package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.TokenValue;

import java.util.List;

public class AST {
    Symbol symbol;
    Production production;
    TokenValue tokenValue;
    List<AST> children;

    public AST(Symbol symbol, Production production, TokenValue tokenValue, AST... children) {
        this.symbol = symbol;
        this.production = production;
        this.tokenValue = tokenValue;
        this.children = List.of(children);
    }

    public AST(Symbol symbol, Production production, TokenValue tokenValue, List<AST> children) {
        this.symbol = symbol;
        this.production = production;
        this.tokenValue = tokenValue;
        this.children = children;
    }
}

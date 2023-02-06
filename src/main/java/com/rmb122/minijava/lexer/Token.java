package com.rmb122.minijava.lexer;

import com.rmb122.minijava.parser.Symbol;
import com.rmb122.minijava.parser.Symbolize;

public class Token implements Symbolize {
    String name;
    int priority;
    Symbol symbol;

    public Token(String name) {
        this.name = name;
        this.priority = 0;
    }

    public Token(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return this.name;
    }

    public Symbol asSymbol() {
        if (symbol == null) {
            this.symbol = new Symbol(this);
        }
        return this.symbol;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token t && this.name.equals(t.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

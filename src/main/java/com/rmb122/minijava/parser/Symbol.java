package com.rmb122.minijava.parser;

import com.rmb122.minijava.lexer.Token;

public class Symbol {
    public static Symbol EMPTY_SYMBOL = new Symbol(false, "ε", true);
    public static Symbol EOF_SYMBOL = new Symbol(false, "#", true);
    public static Symbol EXTEND_START_SYMBOL = new Symbol(false, "__START__", true);

    boolean terminating;
    String name;
    boolean unique;

    // 创建终结符
    public Symbol(Token token) {
        this.terminating = true;
        this.name = token.getName();
        this.unique = false;
    }

    // 创建非终结符
    public Symbol(String name) {
        this.terminating = false;
        this.name = name;
        this.unique = false;
    }

    private Symbol(boolean terminating, String name, boolean unique) {
        this.terminating = terminating;
        this.name = name;
        this.unique = false;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Symbol s && (this.unique ? s.unique : this.name.equals(s.name));
    }

    @Override
    public int hashCode() {
        return this.unique ? super.hashCode() : this.name.hashCode();
    }
}

package com.rmb122.minijs.parser;

import com.rmb122.minijs.lexer.Token;

public class Symbol implements Symbolize {
    public static Symbol EMPTY_SYMBOL = new Symbol(false, "ε", true);
    public static Symbol EOF_SYMBOL = new Symbol(false, "#", true);
    public static Symbol EXTEND_START_SYMBOL = new Symbol(false, "__START__", true);

    boolean terminating;
    String name;
    boolean special;

    // 创建终结符
    public Symbol(Token token) {
        this.terminating = true;
        this.name = token.getName();
        this.special = false;
    }

    // 创建非终结符
    public Symbol(String name) {
        this.terminating = false;
        this.name = name;
        this.special = false;
    }

    private Symbol(boolean terminating, String name, boolean special) {
        this.terminating = terminating;
        this.name = name;
        this.special = special;
    }

    @Override
    public Symbol asSymbol() {
        return this;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Symbol s && this.special == s.special && this.name.equals(s.name);
    }

    @Override
    public int hashCode() {
        return this.special ? super.hashCode() : this.name.hashCode();
    }
}

package com.rmb122.lexer;

public class Token {
    String name;
    int priority;

    public Token(String name, int priority) {
        this.name = name;
        this.priority = priority;
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

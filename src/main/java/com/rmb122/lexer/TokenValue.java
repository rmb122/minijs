package com.rmb122.lexer;

public class TokenValue {
    Token token;
    String value;

    public TokenValue(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public String toString() {
        return "TokenValue{" +
                "token=" + token +
                ", value='" + value + '\'' +
                '}';
    }
}


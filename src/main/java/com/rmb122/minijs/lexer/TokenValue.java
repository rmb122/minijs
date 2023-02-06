package com.rmb122.minijs.lexer;

public class TokenValue {
    Token token;
    String value;
    int lineNum;
    int colNum;

    public TokenValue(Token token, String value, int lineNum, int colNum) {
        this.token = token;
        this.value = value;
        this.lineNum = lineNum;
        this.colNum = colNum;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getColNum() {
        return colNum;
    }

    @Override
    public String toString() {
        return "TokenValue{" +
                "token=" + token +
                ", value='" + value + '\'' +
                ", lineNum=" + lineNum +
                ", colNum=" + colNum +
                '}';
    }
}


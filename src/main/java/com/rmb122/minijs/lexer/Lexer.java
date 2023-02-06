package com.rmb122.minijs.lexer;

import com.rmb122.minijs.regexp.RegexpCompileError;
import com.rmb122.minijs.regexp.RegexpMatchResult;
import com.rmb122.minijs.regexp.RegexpOption;
import com.rmb122.minijs.regexp.RegexpSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Lexer {
    RegexpSet<Token> regexpSet;
    HashSet<Token> tokenSet = new HashSet<>();
    HashSet<Token> ignoredTokenSet = new HashSet<>();
    int currLineNum;
    int currColNum;

    public Lexer(RegexpOption... option) {
        this.regexpSet = new RegexpSet<>(option);
    }

    public void addToken(String pattern, Token token) throws LexerError, RegexpCompileError {
        if (tokenSet.contains(token)) {
            throw new LexerError(String.format("duplicate token found %s", token.name));
        }

        tokenSet.add(token);
        regexpSet.addPattern(pattern, token);
    }

    public void addToken(String pattern, Token token, boolean ignore) throws LexerError, RegexpCompileError {
        this.addToken(pattern, token);
        if (ignore) {
            ignoredTokenSet.add(token);
        }
    }

    public void compile() throws RegexpCompileError {
        regexpSet.compile();
    }

    public List<TokenValue> scan(String input) throws LexerError {
        List<TokenValue> tokens = new ArrayList<>();
        RegexpMatchResult<Token> matchResult;
        int currPos = 0;

        do {
            matchResult = this.regexpSet.matchNext(input, currPos);
            if (matchResult != null) {
                Token maxPriorityToken = null;
                for (Token token : matchResult.containerSet) {
                    // 越小优先级越高
                    if (maxPriorityToken == null) {
                        maxPriorityToken = token;
                    } else if (maxPriorityToken.priority > token.priority) {
                        maxPriorityToken = token;
                    }
                }

                if (maxPriorityToken == null) {
                    throw new LexerError("empty token set found");
                }

                String matchedString = input.substring(currPos, currPos + matchResult.length);
                if (!ignoredTokenSet.contains(maxPriorityToken)) {
                    tokens.add(new TokenValue(maxPriorityToken, matchedString, currLineNum, currColNum));
                }

                for (int i = 0; i < matchedString.length(); i++) {
                    this.currColNum++;

                    if (matchedString.charAt(i) == '\n') {
                        this.currLineNum += 1;
                        this.currColNum = 0;
                    }
                }

                currPos += matchResult.length;
            }
        } while (matchResult != null);

        if (currPos != input.length()) {
            throw new LexerError(String.format("invalid input at pos %d", currPos));
        }

        return tokens;
    }

    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer(RegexpOption.DEBUG);
        lexer.addToken("if", new Token("IF", 0));
        lexer.addToken("else", new Token("ELSE", 0));
        lexer.addToken("[\n\r\t ]+", new Token("BLANK", 0), true);
        lexer.addToken("[0-9]+(\\.[0-9]+)?", new Token("NUMBER", 1));
        lexer.addToken("[A-Za-z_][A-Za-z0-9_]*", new Token("ID", 1));
        lexer.addToken("\"(\\\\.|[^\\\\\"\\n])*\"", new Token("STRING", 1));

        lexer.compile();
        System.out.println(Arrays.toString(lexer.scan("ifelse if else asd 0.1 0.2 0.1123123 1.1 asdasd  \"aasd' a asd asd a  \\.  asd\\\"xsd\" \"\"  \"asdasd\\n!!!!\" \n asd ").toArray()));
    }
}

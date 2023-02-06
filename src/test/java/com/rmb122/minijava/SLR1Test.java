package com.rmb122.minijava;

import com.rmb122.minijava.lexer.Lexer;
import com.rmb122.minijava.lexer.Token;
import com.rmb122.minijava.parser.AST;
import com.rmb122.minijava.parser.Parser;
import com.rmb122.minijava.parser.Production;
import com.rmb122.minijava.parser.Symbol;
import junit.framework.TestCase;

public class SLR1Test extends TestCase {
    public void testSLR1Case1() throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        /*
            NUMBER := [0-9]+

            expr := expr '+' term | term
            term := term '*' factor | factor
            factor := NUMBER | '(' expr ')'
         */

        Token number = new Token("NUMBER", 0);
        Token plus = new Token("PLUS", 0);
        Token multi = new Token("MULTI", 0);
        Token lpar = new Token("LPAR", 0);
        Token rpar = new Token("RPAR", 0);
        Token blank = new Token("BLANK", 0);

        lexer.addToken("[0-9]+", number);
        lexer.addToken("\\+", plus);
        lexer.addToken("\\*", multi);
        lexer.addToken("\\(", lpar);
        lexer.addToken("\\)", rpar);
        lexer.addToken("[\n\r\t ]+", blank, true);

        lexer.compile();

        Symbol expr = new Symbol("expr");
        Symbol term = new Symbol("term");
        Symbol factor = new Symbol("factor");

        parser.addProduction(new Production(expr, expr, plus.asSymbol(), term));
        parser.addProduction(new Production(expr, term));

        parser.addProduction(new Production(term, term, multi.asSymbol(), factor));
        parser.addProduction(new Production(term, factor));

        parser.addProduction(new Production(factor, number.asSymbol()));
        parser.addProduction(new Production(factor, lpar.asSymbol(), expr, rpar.asSymbol()));

        parser.setStartSymbol(expr);
        parser.compile();
        parser.generateParserTableCsv();

        AST result = parser.parse(lexer.scan("(567 * (3 + 3)) * 123 + 1+1 + 123 * (123 * 12 + (1+2)) "));
        System.out.println(result.generateDOTFile());
    }

    public void testSLR1Case2() throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Token a = new Token("a");
        Token b = new Token("b");

        lexer.addToken("a", a);
        lexer.addToken("b", b);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol A = new Symbol("A");
        Symbol B = new Symbol("B");

        parser.addProduction(new Production(S, A, B));
        parser.addProduction(new Production(A, a.asSymbol(), B, a.asSymbol()));
        parser.addProduction(new Production(A));
        parser.addProduction(new Production(B, b.asSymbol(), A, b.asSymbol()));
        parser.addProduction(new Production(B));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();
        AST result = parser.parse(lexer.scan("baab"));
        System.out.println(result.generateDOTFile());
    }
}

package com.rmb122.minijs;

import com.rmb122.minijs.lexer.Lexer;
import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.parser.AST;
import com.rmb122.minijs.parser.Parser;
import com.rmb122.minijs.parser.Production;
import com.rmb122.minijs.parser.Symbol;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class LR0Test extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LR0Test(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(LR0Test.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testLR0() throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Token a = new Token("a");
        Token c = new Token("c");
        Token e = new Token("e");
        Token b = new Token("b");
        Token d = new Token("d");

        lexer.addToken("a", a);
        lexer.addToken("c", c);
        lexer.addToken("e", e);
        lexer.addToken("b", b);
        lexer.addToken("d", d);

        lexer.compile();

        Symbol S = new Symbol("S");
        Symbol A = new Symbol("A");
        Symbol B = new Symbol("B");

        parser.addProduction(new Production(S, a.asSymbol(), A, c.asSymbol(), B, e.asSymbol()));
        parser.addProduction(new Production(A, b.asSymbol()));
        parser.addProduction(new Production(A, A, b.asSymbol()));
        parser.addProduction(new Production(B, d.asSymbol()));

        parser.setStartSymbol(S);
        parser.compile();
        parser.generateParserTableCsv();

        AST result = parser.parse(lexer.scan("abbcde"));
        System.out.println(result.generateDOTFile());
    }
}

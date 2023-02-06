package com.rmb122.minijs;

import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.parser.Parser;
import com.rmb122.minijs.parser.Production;
import com.rmb122.minijs.parser.Symbol;
import junit.framework.TestCase;

public class FirstFollowTest extends TestCase {
    public void testFirstFollow() throws Exception {
        Parser parser = new Parser();

        Token a = new Token("a");
        Token b = new Token("b");
        Token c = new Token("c");

        Symbol S = new Symbol("S");
        Symbol A = new Symbol("A");
        Symbol B = new Symbol("B");
        Symbol C = new Symbol("C");
        Symbol D = new Symbol("D");

        parser.addProduction(new Production(S, A, B));
        parser.addProduction(new Production(S, b.asSymbol(), C));
        parser.addProduction(new Production(A, b.asSymbol()));
        parser.addProduction(new Production(A));
        parser.addProduction(new Production(B, a.asSymbol(), D));
        parser.addProduction(new Production(B));
        parser.addProduction(new Production(C, A, D));
        parser.addProduction(new Production(C, b.asSymbol()));
        parser.addProduction(new Production(D, a.asSymbol(), S));
        parser.addProduction(new Production(D, c.asSymbol()));

        parser.setStartSymbol(S);
        parser.compile();
    }
}

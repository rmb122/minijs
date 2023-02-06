package com.rmb122.minijs;

import com.rmb122.minijs.lexer.Lexer;
import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.parser.AST;
import com.rmb122.minijs.parser.Parser;
import com.rmb122.minijs.parser.Symbol;
import junit.framework.TestCase;

public class JSONTest extends TestCase {
    public void testJSON() throws Exception {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        Token NUMBER = new Token("NUMBER");
        Token STRING = new Token("STRING");
        Token TRUE = new Token("TRUE");
        Token FALSE = new Token("FALSE");
        Token NULL = new Token("NULL");
        Token BLANK = new Token("BLANK");
        Token LSBRACK = new Token("LSBRACK");
        Token RSBRACK = new Token("RSBRACK");
        Token LCBRACK = new Token("LCBRACK");
        Token RCBRACK = new Token("RCBRACK");
        Token COLON = new Token("COLON");
        Token COMMA = new Token("COMMA");

        lexer.addToken("[0-9]+", NUMBER);
        lexer.addToken("\"(\\\\.|[^\"\\\\])*\"", STRING);
        lexer.addToken("true", TRUE);
        lexer.addToken("false", FALSE);
        lexer.addToken("null", NULL);
        lexer.addToken("\\[", LSBRACK);
        lexer.addToken("\\]", RSBRACK);
        lexer.addToken("{", LCBRACK);
        lexer.addToken("}", RCBRACK);
        lexer.addToken(":", COLON);
        lexer.addToken(",", COMMA);
        lexer.addToken("[\n\t\r ]+", BLANK, true);

        Symbol value = new Symbol("value");
        Symbol object = new Symbol("object");
        Symbol array = new Symbol("array");
        Symbol array_items = new Symbol("array_items");
        Symbol object_items = new Symbol("object_items");
        Symbol item_pair = new Symbol("item_pair");

        parser.addProduction(value, object);
        parser.addProduction(value, array);
        parser.addProduction(value, STRING);
        parser.addProduction(value, NUMBER);
        parser.addProduction(value, TRUE);
        parser.addProduction(value, FALSE);
        parser.addProduction(value, NULL);

        parser.addProduction(array, LSBRACK, RSBRACK);
        parser.addProduction(array, LSBRACK, array_items, RSBRACK);

        parser.addProduction(array_items, array_items, COMMA, value);
        parser.addProduction(array_items, value);

        parser.addProduction(object, LCBRACK, RCBRACK);
        parser.addProduction(object, LCBRACK, object_items, RCBRACK);

        parser.addProduction(object_items, object_items, COMMA, item_pair);
        parser.addProduction(object_items, item_pair);

        parser.addProduction(item_pair, STRING, COLON, value);

        parser.setStartSymbol(value);
        lexer.compile();
        parser.compile();
        // parser.generateParserTableCsv();

        AST ast = parser.parse(lexer.scan("""
                {
                    "a": [
                        1
                    ],
                    "b": {
                        "2": 1
                    },
                    "c": true,
                    "d": false,
                    "e": null,
                    "f": [],
                    "g": {},
                    "h": [{"1": "2", "3": 4}, 5, 6, 7]
                }
                """));
        System.out.println(ast.generateDOTFile());

        ast = parser.parse(lexer.scan("""
                "asdasd\\n\\rasd !! asd"
                 """));
        System.out.println(ast.generateDOTFile());
    }
}

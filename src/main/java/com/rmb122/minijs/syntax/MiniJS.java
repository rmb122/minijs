package com.rmb122.minijs.syntax;

import com.rmb122.minijs.lexer.Lexer;
import com.rmb122.minijs.lexer.LexerError;
import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.parser.AST;
import com.rmb122.minijs.parser.Parser;
import com.rmb122.minijs.parser.ParserError;
import com.rmb122.minijs.parser.Symbol;

public class MiniJS {
    private static final Lexer lexer = new Lexer();
    private static final Parser parser = new Parser();

    static {
        // modify from
        // https://hepunx.rl.ac.uk/~adye/jsspec11/llr.htm
        // https://www-archive.mozilla.org/js/language/grammar14.html
        try {
            Token BLANK = new Token("BLANK");
            Token IDENTIFIER = new Token("IDENTIFIER", 1);
            Token FUNCTION = new Token("FUNCTION");
            Token LP = new Token("LP");
            Token RP = new Token("RP");
            Token LB = new Token("LB");
            Token RB = new Token("RB");
            Token LC = new Token("LC");
            Token RC = new Token("RC");
            Token SM = new Token("SM");
            Token ASN = new Token("ASN");
            Token IF = new Token("IF");
            Token ELSE = new Token("ELSE");
            Token WHILE = new Token("WHILE");
            Token FOR = new Token("FOR");
            Token BREAK = new Token("BREAK");
            Token CONTINUE = new Token("CONTINUE");
            Token RETURN = new Token("RETURN");
            Token PLUS = new Token("PLUS");
            Token MINUS = new Token("MINUS");
            Token DIV = new Token("DIV");
            Token MUL = new Token("MUL");
            Token MOD = new Token("MOD");
            Token DOT = new Token("DOT");
            Token LT = new Token("LT");
            Token LTE = new Token("LTE");
            Token GT = new Token("GT");
            Token GTE = new Token("GTE");
            Token CM = new Token("CM");
            Token CN = new Token("CN");
            Token THIS = new Token("THIS");
            Token TRUE = new Token("TRUE");
            Token FALSE = new Token("FALSE");
            Token NULL = new Token("NULL");
            Token INTEGER = new Token("INTEGER");
            Token FLOAT = new Token("FLOAT");
            Token STRING = new Token("STRING");
            Token AND = new Token("AND");
            Token OR = new Token("OR");
            Token DELETE = new Token("DELETE");
            Token NEW = new Token("NEW");
            Token EQ = new Token("EQ");
            Token NE = new Token("NE");
            Token VAR = new Token("VAR");

            lexer.addToken("[\n\r\t ]+", BLANK, true);
            lexer.addToken("[A-Za-z][A-Za-z0-9]*", IDENTIFIER);
            lexer.addToken("function", FUNCTION);
            lexer.addToken("\\(", LP);
            lexer.addToken("\\)", RP);
            lexer.addToken("\\[", LB);
            lexer.addToken("]", RB);
            lexer.addToken("{", LC);
            lexer.addToken("}", RC);
            lexer.addToken(";", SM);
            lexer.addToken("=", ASN);
            lexer.addToken("if", IF);
            lexer.addToken("else", ELSE);
            lexer.addToken("while", WHILE);
            lexer.addToken("for", FOR);
            lexer.addToken("break", BREAK);
            lexer.addToken("continue", CONTINUE);
            lexer.addToken("return", RETURN);
            lexer.addToken("\\+", PLUS);
            lexer.addToken("-", MINUS);
            lexer.addToken("/", DIV);
            lexer.addToken("\\*", MUL);
            lexer.addToken("%", MOD);
            lexer.addToken("\\.", DOT);
            lexer.addToken("<", LT);
            lexer.addToken("<=", LTE);
            lexer.addToken(">", GT);
            lexer.addToken(">=", GTE);
            lexer.addToken(",", CM);
            lexer.addToken(":", CN);
            lexer.addToken("this", THIS);
            lexer.addToken("true", TRUE);
            lexer.addToken("false", FALSE);
            lexer.addToken("null", NULL);
            lexer.addToken("[0-9]+", INTEGER);
            lexer.addToken("[0-9]+\\.[0-9]+", FLOAT);
            lexer.addToken("\"(\\\\.|[^\\\\\"\n])*\"", STRING);
            lexer.addToken("&&", AND);
            lexer.addToken("\\|\\|", OR);
            lexer.addToken("delete", DELETE);
            lexer.addToken("new", NEW);
            lexer.addToken("==", EQ);
            lexer.addToken("!=", NE);
            lexer.addToken("var", VAR);

            Symbol program = new Symbol("program");
            Symbol element = new Symbol("element");
            Symbol parameterListOpt = new Symbol("parameterListOpt");
            Symbol parameterList = new Symbol("parameterList");
            Symbol compoundStatement = new Symbol("compoundStatement");
            Symbol statements = new Symbol("statements");
            Symbol statement = new Symbol("statement");
            Symbol elseStatement = new Symbol("elseStatement");
            Symbol condition = new Symbol("condition");
            Symbol expressionOpt = new Symbol("expressionOpt");
            Symbol expression = new Symbol("expression");
            Symbol assignmentExpression = new Symbol("assignmentExpression");
            Symbol rightHandExpression = new Symbol("rightHandExpression");
            Symbol orExpression = new Symbol("orExpression");
            Symbol andExpression = new Symbol("andExpression");
            Symbol equalityExpression = new Symbol("equalityExpression");
            Symbol relationalExpression = new Symbol("relationalExpression");
            Symbol additiveExpression = new Symbol("additiveExpression");
            Symbol multiplicativeExpression = new Symbol("multiplicativeExpression");
            Symbol unaryExpression = new Symbol("unaryExpression");
            Symbol leftHandExpression = new Symbol("leftHandExpression");
            Symbol memberOperator = new Symbol("memberOperator");
            Symbol callOperator = new Symbol("callOperator");
            Symbol argumentListOpt = new Symbol("argumentListOpt");
            Symbol argumentList = new Symbol("argumentList");
            Symbol primaryExpression = new Symbol("primaryExpression");
            Symbol objectLiteral = new Symbol("objectLiteral");
            Symbol arrayLiteral = new Symbol("arrayLiteral");
            Symbol fieldList = new Symbol("fieldList");
            Symbol literalField = new Symbol("literalField");
            Symbol elementList = new Symbol("elementList");
            Symbol variableDefinition = new Symbol("variableDefinition");
            Symbol variableDeclarationList = new Symbol("variableDeclarationList");
            Symbol variableDeclaration = new Symbol("variableDeclaration");
            Symbol variableInitializer = new Symbol("variableInitializer");

            // program
            parser.addProduction(program);
            parser.addProduction(program, element, program);

            // element
            parser.addProduction(element, FUNCTION, IDENTIFIER, LP, parameterListOpt, RP, compoundStatement);
            parser.addProduction(element, statement);

            // parameterListOpt
            parser.addProduction(parameterListOpt);
            parser.addProduction(parameterListOpt, parameterList);

            // parameterList
            parser.addProduction(parameterList, IDENTIFIER);
            parser.addProduction(parameterList, IDENTIFIER, CM, parameterList);

            // compoundStatement
            parser.addProduction(compoundStatement, LC, statements, RC);

            // statements
            parser.addProduction(statements);
            parser.addProduction(statements, statement, statements);

            // statement
            parser.addProduction(statement, SM);
            parser.addProduction(statement, IF, condition, compoundStatement, elseStatement);
            parser.addProduction(statement, WHILE, condition, compoundStatement);
            parser.addProduction(statement, FOR, LP, expressionOpt, SM, expressionOpt, SM, expressionOpt, RP, compoundStatement);
            parser.addProduction(statement, FOR, LP, variableDefinition, SM, expressionOpt, SM, expressionOpt, RP, compoundStatement);
            parser.addProduction(statement, BREAK, SM);
            parser.addProduction(statement, CONTINUE, SM);
            parser.addProduction(statement, RETURN, expressionOpt, SM);
            parser.addProduction(statement, expression, SM);
            parser.addProduction(statement, variableDefinition, SM);

            // elseStatement
            parser.addProduction(elseStatement);
            parser.addProduction(elseStatement, ELSE, compoundStatement);

            // condition
            parser.addProduction(condition, LP, expression, RP);

            // expressionOpt
            parser.addProduction(expressionOpt, expression);
            parser.addProduction(expressionOpt);

            // expression
            parser.addProduction(expression, assignmentExpression);
            parser.addProduction(expression, assignmentExpression, CM, expression);

            // assignmentExpression
            parser.addProduction(assignmentExpression, rightHandExpression);
            // modify here assignmentExpression
            parser.addProduction(assignmentExpression, leftHandExpression, ASN, assignmentExpression);

            // rightHandExpression
            parser.addProduction(rightHandExpression, orExpression);

            // orExpression
            parser.addProduction(orExpression, andExpression);
            parser.addProduction(orExpression, andExpression, OR, orExpression);

            // andExpression
            parser.addProduction(andExpression, equalityExpression);
            parser.addProduction(andExpression, equalityExpression, AND, andExpression);

            // equalityExpression
            parser.addProduction(equalityExpression, relationalExpression);
            parser.addProduction(equalityExpression, relationalExpression, EQ, equalityExpression);
            parser.addProduction(equalityExpression, relationalExpression, NE, equalityExpression);

            // relationalExpression
            parser.addProduction(relationalExpression, additiveExpression);
            parser.addProduction(relationalExpression, additiveExpression, LT, relationalExpression);
            parser.addProduction(relationalExpression, additiveExpression, LTE, relationalExpression);
            parser.addProduction(relationalExpression, additiveExpression, GT, relationalExpression);
            parser.addProduction(relationalExpression, additiveExpression, GTE, relationalExpression);

            // additiveExpression
            parser.addProduction(additiveExpression, multiplicativeExpression);
            parser.addProduction(additiveExpression, multiplicativeExpression, PLUS, additiveExpression);
            parser.addProduction(additiveExpression, multiplicativeExpression, MINUS, additiveExpression);

            // multiplicativeExpression
            parser.addProduction(multiplicativeExpression, unaryExpression);
            parser.addProduction(multiplicativeExpression, unaryExpression, MUL, multiplicativeExpression);
            parser.addProduction(multiplicativeExpression, unaryExpression, DIV, multiplicativeExpression);
            parser.addProduction(multiplicativeExpression, unaryExpression, MOD, multiplicativeExpression);

            // unaryExpression
            parser.addProduction(unaryExpression, leftHandExpression);
            parser.addProduction(unaryExpression, MINUS, unaryExpression);
            parser.addProduction(unaryExpression, NEW, leftHandExpression);
            parser.addProduction(unaryExpression, DELETE, leftHandExpression);

            // leftHandExpression
            parser.addProduction(leftHandExpression, primaryExpression);
            parser.addProduction(leftHandExpression, leftHandExpression, memberOperator);
            parser.addProduction(leftHandExpression, leftHandExpression, callOperator);

            // memberOperator
            parser.addProduction(memberOperator, DOT, IDENTIFIER);
            parser.addProduction(memberOperator, LB, expression, RB);

            // callOperator
            parser.addProduction(callOperator, LP, argumentListOpt, RP);

            // argumentListOpt
            parser.addProduction(argumentListOpt);
            parser.addProduction(argumentListOpt, argumentList);

            // argumentList
            parser.addProduction(argumentList, assignmentExpression, CM, argumentList);
            parser.addProduction(argumentList, assignmentExpression);

            // primaryExpression
            parser.addProduction(primaryExpression, LP, expression, RP);
            parser.addProduction(primaryExpression, IDENTIFIER);
            parser.addProduction(primaryExpression, INTEGER);
            parser.addProduction(primaryExpression, FLOAT);
            parser.addProduction(primaryExpression, STRING);
            parser.addProduction(primaryExpression, FALSE);
            parser.addProduction(primaryExpression, TRUE);
            parser.addProduction(primaryExpression, NULL);
            parser.addProduction(primaryExpression, THIS);
            parser.addProduction(primaryExpression, objectLiteral);
            parser.addProduction(primaryExpression, arrayLiteral);

            // objectLiteral
            parser.addProduction(objectLiteral, LC, RC);
            parser.addProduction(objectLiteral, LC, fieldList, RC);

            // fieldList
            parser.addProduction(fieldList, literalField);
            parser.addProduction(fieldList, fieldList, CM, literalField);

            // literalField
            parser.addProduction(literalField, IDENTIFIER, CN, assignmentExpression);
            parser.addProduction(literalField, STRING, CN, assignmentExpression);

            // arrayLiteral
            parser.addProduction(arrayLiteral, LB, RB);
            parser.addProduction(arrayLiteral, LB, elementList, RB);

            // elementList
            parser.addProduction(elementList, assignmentExpression);
            parser.addProduction(elementList, elementList, CM, assignmentExpression);

            // variableDefinition
            parser.addProduction(variableDefinition, VAR, variableDeclarationList);

            // variableDeclarationList
            parser.addProduction(variableDeclarationList, variableDeclaration);
            parser.addProduction(variableDeclarationList, variableDeclarationList, CM, variableDeclaration);

            // variableDeclaration
            parser.addProduction(variableDeclaration, IDENTIFIER, variableInitializer);

            // variableInitializer
            parser.addProduction(variableInitializer);
            parser.addProduction(variableInitializer, ASN, assignmentExpression);

            lexer.compile();

            parser.setStartSymbol(program);
            parser.compile();

            // parser.generateParserTableCsv();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AST parse(String input) throws LexerError, ParserError {
        return parser.parse(lexer.scan(input));
    }

    public static void main(String[] args) throws Exception {
        AST ast = MiniJS.parse("""
                function aa () {
                    var a = 123;
                    var b = 44 * (a.b()), c = 123123 - 132;
                    for (var a = 1; a < 1; a = a + 1) {}
                    
                    this.a;
                    true;
                    false;
                    null;
                    b = [123, 123, asd()];
                }
                
                function test(a, b, c, d) {
                    a = 123;
                    b[a] = a;
                    
                    for (a = 1; i < 3; i = i + 1) {
                        asd = 1;
                    }
                    
                    for (;;) {}
                    
                    if (a == 1) {}
                    
                    b = -1;
                    b = -asd;
                    c = c -- c;
                    
                    d = a * b + c - e / f % d;
                    if (a != 1) {} else {}
                    
                    while (a <= 1) {
                        asd = 1;
                        break;
                        continue;
                    }
                    
                    if (a && b || c) {
                        1.1;
                    }
                    
                    new a;
                    delete a;
                    
                    return 123;
                    return ;
                }
                
               
                b = test;
                (b)(a, 123, 3 + 1)[asd].asd = {"asd": "asd", a: null};
                """);
        System.out.println(ast.generateDOTFile());
    }
}

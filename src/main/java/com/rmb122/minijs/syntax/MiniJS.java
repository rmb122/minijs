package com.rmb122.minijs.syntax;

import com.rmb122.minijs.lexer.Lexer;
import com.rmb122.minijs.lexer.LexerError;
import com.rmb122.minijs.lexer.Token;
import com.rmb122.minijs.parser.AST;
import com.rmb122.minijs.parser.Parser;
import com.rmb122.minijs.parser.ParserError;
import com.rmb122.minijs.parser.Symbol;
import com.rmb122.minijs.vm.Program;
import com.rmb122.minijs.vm.eval.*;
import com.rmb122.minijs.vm.object.*;

import java.util.ArrayList;
import java.util.List;

public class MiniJS {
    private static final Lexer lexer = new Lexer();
    private static final Parser parser = new Parser();

    public static final Token BLANK = new Token("BLANK");
    public static final Token COMMENT = new Token("COMMENT");
    public static final Token MULTI_COMMENT = new Token("MULTI_COMMENT");
    public static final Token IDENTIFIER = new Token("IDENTIFIER", 1);
    public static final Token FUNCTION = new Token("FUNCTION");
    public static final Token LP = new Token("LP");
    public static final Token RP = new Token("RP");
    public static final Token LB = new Token("LB");
    public static final Token RB = new Token("RB");
    public static final Token LC = new Token("LC");
    public static final Token RC = new Token("RC");
    public static final Token SM = new Token("SM");
    public static final Token ASN = new Token("ASN");
    public static final Token IF = new Token("IF");
    public static final Token ELSE = new Token("ELSE");
    public static final Token WHILE = new Token("WHILE");
    public static final Token FOR = new Token("FOR");
    public static final Token BREAK = new Token("BREAK");
    public static final Token CONTINUE = new Token("CONTINUE");
    public static final Token RETURN = new Token("RETURN");
    public static final Token PLUS = new Token("PLUS");
    public static final Token MINUS = new Token("MINUS");
    public static final Token DIV = new Token("DIV");
    public static final Token MUL = new Token("MUL");
    public static final Token MOD = new Token("MOD");
    public static final Token DOT = new Token("DOT");
    public static final Token LT = new Token("LT");
    public static final Token LTE = new Token("LTE");
    public static final Token GT = new Token("GT");
    public static final Token GTE = new Token("GTE");
    public static final Token CM = new Token("CM");
    public static final Token CN = new Token("CN");
    public static final Token THIS = new Token("THIS");
    public static final Token TRUE = new Token("TRUE");
    public static final Token FALSE = new Token("FALSE");
    public static final Token NULL = new Token("NULL");
    public static final Token INTEGER = new Token("INTEGER");
    public static final Token FLOAT = new Token("FLOAT");
    public static final Token STRING = new Token("STRING");
    public static final Token AND = new Token("AND");
    public static final Token OR = new Token("OR");
    public static final Token DELETE = new Token("DELETE");
    public static final Token NEW = new Token("NEW");
    public static final Token EQ = new Token("EQ");
    public static final Token NE = new Token("NE");
    public static final Token VAR = new Token("VAR");

    public static final Symbol program = new Symbol("program");
    public static final Symbol function = new Symbol("function");
    public static final Symbol parameterListOpt = new Symbol("parameterListOpt");
    public static final Symbol parameterList = new Symbol("parameterList");
    public static final Symbol compoundStatement = new Symbol("compoundStatement");
    public static final Symbol statements = new Symbol("statements");
    public static final Symbol statement = new Symbol("statement");
    public static final Symbol elseStatement = new Symbol("elseStatement");
    public static final Symbol condition = new Symbol("condition");
    public static final Symbol expressionListOpt = new Symbol("expressionListOpt");
    public static final Symbol expressionList = new Symbol("expressionList");
    public static final Symbol assignmentExpression = new Symbol("assignmentExpression");
    public static final Symbol rightHandExpression = new Symbol("rightHandExpression");
    public static final Symbol orExpression = new Symbol("orExpression");
    public static final Symbol andExpression = new Symbol("andExpression");
    public static final Symbol equalityExpression = new Symbol("equalityExpression");
    public static final Symbol relationalExpression = new Symbol("relationalExpression");
    public static final Symbol additiveExpression = new Symbol("additiveExpression");
    public static final Symbol multiplicativeExpression = new Symbol("multiplicativeExpression");
    public static final Symbol unaryExpression = new Symbol("unaryExpression");
    public static final Symbol leftHandExpression = new Symbol("leftHandExpression");
    public static final Symbol memberOperator = new Symbol("memberOperator");
    public static final Symbol callOperator = new Symbol("callOperator");
    public static final Symbol argumentListOpt = new Symbol("argumentListOpt");
    public static final Symbol argumentList = new Symbol("argumentList");
    public static final Symbol primaryExpression = new Symbol("primaryExpression");
    public static final Symbol objectLiteral = new Symbol("objectLiteral");
    public static final Symbol arrayLiteral = new Symbol("arrayLiteral");
    public static final Symbol fieldList = new Symbol("fieldList");
    public static final Symbol literalField = new Symbol("literalField");
    public static final Symbol elementList = new Symbol("elementList");
    public static final Symbol variableDefinition = new Symbol("variableDefinition");
    public static final Symbol variableDeclarationList = new Symbol("variableDeclarationList");
    public static final Symbol variableDeclaration = new Symbol("variableDeclaration");
    public static final Symbol variableInitializer = new Symbol("variableInitializer");

    static {
        // modify from
        // https://hepunx.rl.ac.uk/~adye/jsspec11/llr.htm
        // https://www-archive.mozilla.org/js/language/grammar14.html
        try {
            lexer.addToken("[\n\r\t ]+", BLANK, true);
            lexer.addToken("//.*", COMMENT, true);
            lexer.addToken("/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/", MULTI_COMMENT, true);
            lexer.addToken("[A-Za-z][A-Za-z0-9_]*", IDENTIFIER);
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
            lexer.addToken("\"(\\\\.|[^\\\\\"\n])*\"|'(\\\\.|[^\\\\'\n])*'", STRING);
            lexer.addToken("&&", AND);
            lexer.addToken("\\|\\|", OR);
            lexer.addToken("delete", DELETE);
            lexer.addToken("new", NEW);
            lexer.addToken("==", EQ);
            lexer.addToken("!=", NE);
            lexer.addToken("var", VAR);

            // program
            parser.addProduction(ast -> ast.setCustom(new Program()), program);
            parser.addProduction(ast -> {
                Program program = (Program) ast.getChildrenCustom(0);
                program.addFunction((JFunction) ast.getChildrenCustom(1));
                ast.setCustom(program);
            }, program, program, function);
            parser.addProduction(ast -> {
                Program program = (Program) ast.getChildrenCustom(0);
                program.addStmt((Stmt) ast.getChildrenCustom(1));
                ast.setCustom(program);
            }, program, program, statement);

            // element
            parser.addProduction(ast -> ast.setCustom(new JFunction(ast.getChildrenTokenValue(1), (List<String>) ast.getChildrenCustom(3), (StmtList) ast.getChildrenCustom(5))), function, FUNCTION, IDENTIFIER, LP, parameterListOpt, RP, compoundStatement);

            // parameterListOpt
            parser.addProduction(ast -> ast.setCustom(new ArrayList<String>()), parameterListOpt);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), parameterListOpt, parameterList);

            // parameterList
            parser.addProduction(ast -> {
                ArrayList<String> params = new ArrayList<>();
                params.add(ast.getChildrenTokenValue(0));
                ast.setCustom(params);
            }, parameterList, IDENTIFIER);
            parser.addProduction(ast -> {
                ArrayList<String> params = (ArrayList<String>) ast.getChildrenCustom(0);
                params.add(ast.getChildrenTokenValue(2));
                ast.setCustom(params);
            }, parameterList, parameterList, CM, IDENTIFIER);

            // compoundStatement
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), compoundStatement, LC, statements, RC);

            // statements
            parser.addProduction(ast -> ast.setCustom(new StmtList()), statements);
            parser.addProduction(ast -> {
                StmtList stmtList = (StmtList) ast.getChildrenCustom(0);
                stmtList.addStmt((Stmt) ast.getChildrenCustom(1));
                ast.setCustom(stmtList);
            }, statements, statements, statement);

            // statement
            // 不允许空语句
            // parser.addProduction(statement, SM);
            parser.addProduction(ast -> ast.setCustom(new IfStmt((ExprList) ast.getChildrenCustom(1), (StmtList) ast.getChildrenCustom(2), (StmtList) ast.getChildrenCustom(3))), statement, IF, condition, compoundStatement, elseStatement);
            parser.addProduction(ast -> ast.setCustom(new WhileStmt((ExprList) ast.getChildrenCustom(1), (StmtList) ast.getChildrenCustom(2))), statement, WHILE, condition, compoundStatement);
            parser.addProduction(ast -> ast.setCustom(new ForStmt((ExprList) ast.getChildrenCustom(2), (ExprList) ast.getChildrenCustom(4), (ExprList) ast.getChildrenCustom(6), (StmtList) ast.getChildrenCustom(8))), statement, FOR, LP, expressionListOpt, SM, expressionListOpt, SM, expressionListOpt, RP, compoundStatement);
            parser.addProduction(ast -> ast.setCustom(new ForStmt((VariableDefinitionStmt) ast.getChildrenCustom(2), (ExprList) ast.getChildrenCustom(4), (ExprList) ast.getChildrenCustom(6), (StmtList) ast.getChildrenCustom(8))), statement, FOR, LP, variableDefinition, SM, expressionListOpt, SM, expressionListOpt, RP, compoundStatement);
            parser.addProduction(ast -> ast.setCustom(new BreakStmt()), statement, BREAK, SM);
            parser.addProduction(ast -> ast.setCustom(new ContinueStmt()), statement, CONTINUE, SM);
            parser.addProduction(ast -> ast.setCustom(new ReturnStmt((Expr) ast.getChildrenCustom(1))), statement, RETURN, expressionListOpt, SM);
            parser.addProduction(ast -> ast.setCustom(new ExprStmt((Expr) ast.getChildrenCustom(0))), statement, expressionList, SM);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), statement, variableDefinition, SM);

            // elseStatement
            parser.addProduction(ast -> ast.setCustom(new StmtList()), elseStatement);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), elseStatement, ELSE, compoundStatement);

            // condition
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), condition, LP, expressionList, RP);

            // expressionOpt
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), expressionListOpt, expressionList);
            parser.addProduction(ast -> ast.setCustom(new ExprList()), expressionListOpt);

            // expression
            parser.addProduction(ast -> {
                ExprList exprList = new ExprList();
                exprList.addExpr((Expr) ast.getChildrenCustom(0));
                ast.setCustom(exprList);
            }, expressionList, assignmentExpression);
            parser.addProduction(ast -> {
                ExprList exprList = (ExprList) ast.getChildrenCustom(0);
                exprList.addExpr((Expr) ast.getChildrenCustom(2));
                ast.setCustom(exprList);
            }, expressionList, expressionList, CM, assignmentExpression);

            // assignmentExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), assignmentExpression, rightHandExpression);
            // modify here assignmentExpression
            parser.addProduction(ast -> ast.setCustom(new AssigmentExpr((LeftHandExpr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2))), assignmentExpression, leftHandExpression, ASN, assignmentExpression);

            // rightHandExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), rightHandExpression, orExpression);

            // orExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), orExpression, andExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.OR)), orExpression, orExpression, OR, andExpression);

            // andExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), andExpression, equalityExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.AND)), andExpression, andExpression, AND, equalityExpression);

            // equalityExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), equalityExpression, relationalExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.EQ)), equalityExpression, equalityExpression, EQ, relationalExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.NE)), equalityExpression, equalityExpression, NE, relationalExpression);

            // relationalExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), relationalExpression, additiveExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.LT)), relationalExpression, relationalExpression, LT, additiveExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.LTE)), relationalExpression, relationalExpression, LTE, additiveExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.GT)), relationalExpression, relationalExpression, GT, additiveExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.GTE)), relationalExpression, relationalExpression, GTE, additiveExpression);

            // additiveExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), additiveExpression, multiplicativeExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.ADD)), additiveExpression, additiveExpression, PLUS, multiplicativeExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.SUB)), additiveExpression, additiveExpression, MINUS, multiplicativeExpression);

            // multiplicativeExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), multiplicativeExpression, unaryExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.MUL)), multiplicativeExpression, multiplicativeExpression, MUL, unaryExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.DIV)), multiplicativeExpression, multiplicativeExpression, DIV, unaryExpression);
            parser.addProduction(ast -> ast.setCustom(new BinaryExpr((Expr) ast.getChildrenCustom(0), (Expr) ast.getChildrenCustom(2), BinaryExpr.Op.MOD)), multiplicativeExpression, multiplicativeExpression, MOD, unaryExpression);

            // unaryExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), unaryExpression, leftHandExpression);
            parser.addProduction(ast -> ast.setCustom(new UnaryExpr((Expr) ast.getChildrenCustom(1), UnaryExpr.Op.MINUS)), unaryExpression, MINUS, unaryExpression);
            // 不能 new new a.b().c()
            parser.addProduction(ast -> ast.setCustom(new UnaryExpr((Expr) ast.getChildrenCustom(1), UnaryExpr.Op.NEW)), unaryExpression, NEW, leftHandExpression);
            parser.addProduction(ast -> ast.setCustom(new UnaryExpr((Expr) ast.getChildrenCustom(1), UnaryExpr.Op.DELETE)), unaryExpression, DELETE, leftHandExpression);

            // leftHandExpression
            parser.addProduction(ast -> ast.setCustom(new LeftHandExpr((Expr) ast.getChildrenCustom(0))), leftHandExpression, primaryExpression);
            parser.addProduction(ast -> {
                LeftHandExpr leftHandExpr = (LeftHandExpr) ast.getChildrenCustom(0);
                leftHandExpr.addOperator((LeftHandOperator) ast.getChildrenCustom(1));
                ast.setCustom(leftHandExpr);
            }, leftHandExpression, leftHandExpression, memberOperator);
            parser.addProduction(ast -> {
                LeftHandExpr leftHandExpr = (LeftHandExpr) ast.getChildrenCustom(0);
                leftHandExpr.addOperator((LeftHandOperator) ast.getChildrenCustom(1));
                ast.setCustom(leftHandExpr);
            }, leftHandExpression, leftHandExpression, callOperator);

            // memberOperator
            // a.xx 等效于 a['xx']
            parser.addProduction(ast -> ast.setCustom(new MemberOperator(new Variable(new JString(ast.getChildrenTokenValue(1))))), memberOperator, DOT, IDENTIFIER);
            parser.addProduction(ast -> ast.setCustom(new MemberOperator((Expr) ast.getChildrenCustom(1))), memberOperator, LB, expressionList, RB);

            // callOperator
            parser.addProduction(ast -> {
                ast.setCustom(new CallOperator((ArrayList<Expr>) ast.getChildrenCustom(1)));
            }, callOperator, LP, argumentListOpt, RP);

            // argumentListOpt
            parser.addProduction(ast -> ast.setCustom(new ArrayList<Expr>()), argumentListOpt);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), argumentListOpt, argumentList);

            // argumentList
            parser.addProduction(ast -> {
                ArrayList<Expr> args = (ArrayList<Expr>) ast.getChildrenCustom(0);
                args.add((Expr) ast.getChildrenCustom(2));
                ast.setCustom(args);
            }, argumentList, argumentList, CM, assignmentExpression);
            parser.addProduction(ast -> {
                ArrayList<Expr> args = new ArrayList<>();
                args.add((Expr) ast.getChildrenCustom(0));
                ast.setCustom(args);
            }, argumentList, assignmentExpression);

            // primaryExpression
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), primaryExpression, LP, expressionList, RP);
            parser.addProduction(ast -> ast.setCustom(new Variable(ast.getChildrenTokenValue(0))), primaryExpression, IDENTIFIER);
            parser.addProduction(ast -> ast.setCustom(new Variable(JNumber.fromTokenValue(ast.getChildrenTokenValue(0)))), primaryExpression, INTEGER);
            parser.addProduction(ast -> ast.setCustom(new Variable(JNumber.fromTokenValue(ast.getChildrenTokenValue(0)))), primaryExpression, FLOAT);
            parser.addProduction(ast -> ast.setCustom(new Variable(JString.fromTokenValue(ast.getChildrenTokenValue(0)))), primaryExpression, STRING);
            parser.addProduction(ast -> ast.setCustom(new Variable(JBoolean.FALSE)), primaryExpression, FALSE);
            parser.addProduction(ast -> ast.setCustom(new Variable(JBoolean.TRUE)), primaryExpression, TRUE);
            parser.addProduction(ast -> ast.setCustom(new Variable(JNull.NULL)), primaryExpression, NULL);
            parser.addProduction(ast -> ast.setCustom(Variable.THIS), primaryExpression, THIS);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), primaryExpression, objectLiteral);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(0)), primaryExpression, arrayLiteral);

            // objectLiteral
            parser.addProduction(ast -> ast.setCustom(new ObjectLiteralExpr()), objectLiteral, LC, RC);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), objectLiteral, LC, fieldList, RC);

            // fieldList
            parser.addProduction(ast -> {
                ObjectLiteralExpr objectLiteral = new ObjectLiteralExpr();
                objectLiteral.addLiteral((JString) ast.getChildrenCustom(0), (Expr) ast.children.get(0).getChildrenCustom(2));
                ast.setCustom(objectLiteral);
            }, fieldList, literalField);
            parser.addProduction(ast -> {
                ObjectLiteralExpr objectLiteral = (ObjectLiteralExpr) ast.getChildrenCustom(0);
                objectLiteral.addLiteral((JString) ast.getChildrenCustom(2), (Expr) ast.children.get(2).getChildrenCustom(2));
                ast.setCustom(objectLiteral);
            }, fieldList, fieldList, CM, literalField);

            // literalField
            parser.addProduction(ast -> ast.setCustom(new JString(ast.getChildrenTokenValue(0))), literalField, IDENTIFIER, CN, assignmentExpression);
            parser.addProduction(ast -> ast.setCustom(JString.fromTokenValue(ast.getChildrenTokenValue(0))), literalField, STRING, CN, assignmentExpression);

            // arrayLiteral
            parser.addProduction(ast -> ast.setCustom(new ArrayLiteralExpr()), arrayLiteral, LB, RB);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), arrayLiteral, LB, elementList, RB);

            // elementList
            parser.addProduction(ast -> {
                ArrayLiteralExpr arrayLiteral = new ArrayLiteralExpr();
                arrayLiteral.addLiteral((Expr) ast.getChildrenCustom(0));
                ast.setCustom(arrayLiteral);
            }, elementList, assignmentExpression);
            parser.addProduction(ast -> {
                ArrayLiteralExpr arrayLiteral = (ArrayLiteralExpr) ast.getChildrenCustom(0);
                arrayLiteral.addLiteral((Expr) ast.getChildrenCustom(2));
                ast.setCustom(arrayLiteral);
            }, elementList, elementList, CM, assignmentExpression);

            // variableDefinition
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), variableDefinition, VAR, variableDeclarationList);

            // variableDeclarationList
            parser.addProduction(ast -> {
                VariableDefinitionStmt variableDefinitionStmt = new VariableDefinitionStmt();
                variableDefinitionStmt.addVariableDefinition((VariableDefinitionStmt.VariableDefinition) ast.getChildrenCustom(0));
                ast.setCustom(variableDefinitionStmt);
            }, variableDeclarationList, variableDeclaration);
            parser.addProduction(ast -> {
                VariableDefinitionStmt variableDefinitionStmt = (VariableDefinitionStmt) ast.getChildrenCustom(0);
                variableDefinitionStmt.addVariableDefinition((VariableDefinitionStmt.VariableDefinition) ast.getChildrenCustom(2));
                ast.setCustom(variableDefinitionStmt);
            }, variableDeclarationList, variableDeclarationList, CM, variableDeclaration);

            // variableDeclaration
            parser.addProduction(ast -> ast.setCustom(new VariableDefinitionStmt.VariableDefinition(ast.getChildrenTokenValue(0), (Expr) ast.getChildrenCustom(1))), variableDeclaration, IDENTIFIER, variableInitializer);

            // variableInitializer
            parser.addProduction(ast -> ast.setCustom(null), variableInitializer);
            parser.addProduction(ast -> ast.setCustom(ast.getChildrenCustom(1)), variableInitializer, ASN, assignmentExpression);

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
                /*a
                comment  **
                asd */
                                
                // asdasd asd asd
                //
                a = 1;
                                
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
                (b)(a, 123, 3 + 1)[asd].asd = {"asd": "asd", a: null}['asd'];
                """);
        System.out.println(ast.generateDOTFile());

        ast = MiniJS.parse("""
                return 1 + 1 * 199 / 2;
                """);
        System.out.println(ast.generateDOTFile());
        System.out.println(((Program) ast.getCustom()).eval().toJString().toString());
    }
}

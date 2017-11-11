package parser;
import java.util.*;
import java.util.function.BooleanSupplier;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    Token token;          // current token from the input stream
    Lexer lexer;

    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }

    //match
    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            //next token
            token = lexer.next();
        else
            error(t);
        return value;
    }

    //error
    private void error(TokenType toke) {
        System.err.println("Syntax error: expecting: " + toke
                + "; saw: " + token);
        System.exit(1);
    }

    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok
                + "; saw: " + token);
        System.exit(1);
    }

    // student exercise
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                TokenType.LeftParen, TokenType.RightParen};
        // int main() check
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        // '{' check
        match(TokenType.LeftBrace);
        // call Declarations
        Declarations decls = declarations();
        // call Statements
        Block stmts = programstatements();
        // '}' check
        match(TokenType.RightBrace);
        return new Program (decls, stmts);
    }

    // student exercise
    private Declarations declarations() {
        // Declarations --> { Declaration }
        Declarations decls = new Declarations();
        while(isType()){
            declaration(decls);
        }
        return decls;
    }

    // student exercise
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        Declaration decl;
        Type t = type();
        Variable var;
        // create var match : enum -> string
        var = new Variable(match(TokenType.Identifier));
        // make declaration
        decl = new Declaration(var, t);
        //declarations ArrayList add declaration
        ds.add(decl);

        while(token.type().equals(TokenType.Comma)){
            token = lexer.next();
            var = new Variable(match(TokenType.Identifier));
            decl = new Declaration(var, t);
            ds.add(decl);
        }
        match(TokenType.Semicolon);
    }

    // student exercise
    private Type type () {
        // Type  -->  int | bool | float | char
        Type t = null;
        if(token.type().equals(TokenType.Int)){
            t = Type.INT;
        }else if(token.type().equals(TokenType.Bool)){
            t = Type.BOOL;
        }else if(token.type().equals(TokenType.Float)){
            t = Type.FLOAT;
        }else if(token.type().equals(TokenType.Char)){
            t = Type.CHAR;
        }else{
            error("1");
        }
        token = lexer.next();
        return t;
    }

    // student exercise
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;
        if(token.type().equals(TokenType.Semicolon)){
            s = new Skip();
        }else if(token.type().equals(TokenType.LeftBrace)){
            s = statements();
        }else if(token.type().equals(TokenType.If)){
            s = ifStatement();
        }else if(token.type().equals(TokenType.While)){
            s = whileStatement();
        }else if(token.type().equals(TokenType.Identifier)){
            s = assignment();
        }else{
            error("2");
        }
        return s;
    }

    private Block programstatements () {
        // Block --> '{' Statements '}'
        Statement stmt;
        Block b = new Block();
        //match(TokenType.LeftBrace);

        while(isStatement()){
            stmt = statement();
            b.members.add(stmt);
        }
        //match(TokenType.RightBrace);
        // student exercise
        return b;
    }

    private Block statements () {
        // Block --> '{' Statements '}'
        Statement stmt;
        Block b = new Block();
        match(TokenType.LeftBrace);

        while(isStatement()){
            stmt = statement();
            b.members.add(stmt);
        }
        match(TokenType.RightBrace);
        // student exercise
        return b;
    }

    //is Statement?
    private boolean isStatement() {
        return 	token.type().equals(TokenType.Semicolon) ||
                token.type().equals(TokenType.LeftBrace) ||
                token.type().equals(TokenType.If) ||
                token.type().equals(TokenType.While) ||
                token.type().equals(TokenType.Identifier);
    }
    // student exercise
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        Assignment assignment;
        Variable var = new Variable(match(TokenType.Identifier));
        match(TokenType.Assign);
        Expression expr = expression();
        assignment = new Assignment(var,expr);
        match(TokenType.Semicolon);
        return assignment;
    }

    // student exercise
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional condition;
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression expr = expression();
        match(TokenType.RightParen);
        Statement stmt = statement();
        if(token.type().equals(TokenType.Else)){
            Statement stmt2 = statement();
            condition = new Conditional(expr,stmt,stmt2);
        }else{
            condition = new Conditional(expr,stmt);
        }
        return condition;
    }

    // student
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        Loop loop;
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression expr = expression();
        match(TokenType.RightParen);
        Statement stmt = statement();
        loop = new Loop(expr,stmt);
        return loop;  // student exercise
    }

    // student exercise
    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression expr = conjunction();
        while(token.type().equals(TokenType.Or)){
            Operator operator = new Operator(match(token.type()));
            Expression expr2 = expression();
            expr = new Binary(operator, expr, expr2);
        }
        return expr;
    }

    // student exercise
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression conj = equality();
        while(token.type().equals(TokenType.And)){
            Operator operator = new Operator(match(token.type()));
            Expression conj2 = conjunction();
            conj = new Binary(operator, conj, conj2);
        }
        return conj;
    }

    // student exercise
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression equal = relation();
        if(isEqualityOp()){
            Operator operator = new Operator(match(token.type()));
            Expression equal2 = relation();
            equal = new Binary(operator, equal, equal2);
        }
        return equal;
    }

    // student exercise
    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
        Expression rel = addition();
        if(isRelationalOp()){
            Operator operator = new Operator(match(token.type()));
            Expression rel2 = addition();
            rel = new Binary(operator, rel, rel2);
        }
        return rel;
    }

    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }

    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    // student exercise
    private Value literal( ) {
        Value value = null;
        String data = token.value();
        if(token.type().equals(TokenType.IntLiteral)){
            value = new IntValue(Integer.parseInt(data));
            token = lexer.next();
        }else if(token.type().equals(TokenType.FloatLiteral)){
            value = new FloatValue(Float.parseFloat(data));
            token = lexer.next();
        }else if(token.type().equals(TokenType.CharLiteral)){
            value = new CharValue(data.charAt(0));
            token = lexer.next();
        }else if(token.type().equals(TokenType.True)){
            value = new BoolValue(true);
            token = lexer.next();
        }else if(token.type().equals(TokenType.False)){
            value = new BoolValue(false);
            token = lexer.next();
        }else{
            error("3");
        }
        return value;
    }


    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
                token.type().equals(TokenType.Minus);
    }

    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
                token.type().equals(TokenType.Divide);
    }

    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
                token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
                token.type().equals(TokenType.NotEqual);
    }

    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
                token.type().equals(TokenType.LessEqual) ||
                token.type().equals(TokenType.Greater) ||
                token.type().equals(TokenType.GreaterEqual);
    }

    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
                || token.type().equals(TokenType.Bool)
                || token.type().equals(TokenType.Float)
                || token.type().equals(TokenType.Char);
    }

    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
                isBooleanLiteral() ||
                token.type().equals(TokenType.FloatLiteral) ||
                token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
                token.type().equals(TokenType.False);
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer("test.txt"));
        Program prog = parser.program();
        prog.display(0);           // display abstract syntax tree student
    } //main

} // Parser

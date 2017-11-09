package parser;
import java.util.*;
import lexer.Lexer;
import token.*;

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
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
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
        Block stmts = statements();
        // '}' check
        match(TokenType.RightBrace);
        return new Program (decls, stmts);
    }

    // student exercise
    private Declarations declarations () {
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
            error("Error");
        }
        token = lexer.next();
        return t;          
    }

    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = new Skip();
        // student exercise
        return s;
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        // student exercise
        return b;
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();

        // prog.display();           // display abstract syntax tree student
    } //main

} // Parser

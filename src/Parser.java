public class Parser {
    // Recursive descent parser that inputs a C++Lite program and
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    Token token;          // current token from the input stream
    Lexer lexer;

    // parser constructor
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }

    //match (if true lexer.next)
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

    //error
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
        Block stmts = programStatements();
        // '}' check
        match(TokenType.RightBrace);
        return new Program (decls, stmts);
    }


    //declarations
    // student exercise
    private Declarations declarations() {
        // Declarations --> { Declaration }
        Declarations decls = new Declarations();
        //until not type
        while(isType()){
            // one declaration
            declaration(decls);
        }
        return decls;
    }

    // declaration
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

        //if token==comma loop add
        while(token.type().equals(TokenType.Comma)){
            token = lexer.next();
            var = new Variable(match(TokenType.Identifier));
            decl = new Declaration(var, t);
            ds.add(decl);
        }
        match(TokenType.Semicolon);
    }

    // call type(lexer.next)
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

    // statement
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

    // program statements not need match
    // student exercise
    private Block programStatements () {
        // Block --> '{' Statements '}'
        Statement stmt;
        Block b = new Block();
        // ; | ( | if | while | identifier
        while(isStatement()){
            stmt = statement();
            b.members.add(stmt);
        }
        return b;
    }

    //is Statement
    private boolean isStatement() {
        return 	token.type().equals(TokenType.Semicolon) ||
                token.type().equals(TokenType.LeftBrace) ||
                token.type().equals(TokenType.If) ||
                token.type().equals(TokenType.While) ||
                token.type().equals(TokenType.Identifier);
    }

    //statements need match
    // student exercise
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
        return b;
    }

    //Assignment
    // student exercise
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
        Assignment assignment;
        // Identifier
        Variable var = new Variable(match(TokenType.Identifier));
        // =
        match(TokenType.Assign);
        // Expression
        Expression expr = expression();
        // ;
        match(TokenType.Semicolon);
        assignment = new Assignment(var,expr);
        return assignment;
    }

    // if statement
    // student exercise
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional condition;
        // if(expr) stmt
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression expr = expression();
        match(TokenType.RightParen);
        Statement stmt = statement();
        // token == else
        if(token.type().equals(TokenType.Else)){
            match(TokenType.Else);
            //statement
            Statement stmt2 = statement();
            //exist else
            condition = new Conditional(expr,stmt,stmt2);

        }else{
            //not exist else
            condition = new Conditional(expr,stmt);
        }
        return condition;
    }

    //while
    // student
    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        Loop loop;
        //while(expr) stmt
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
            // expression() because recursive
            Expression expr2 = expression();
            expr = new Binary(operator, expr, expr2);
        }
        return expr;
    }

    // conjunction
    // student exercise
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression conj = equality();
        while(token.type().equals(TokenType.And)){
            Operator operator = new Operator(match(token.type()));
            //recursive
            Expression conj2 = conjunction();
            conj = new Binary(operator, conj, conj2);
        }
        return conj;
    }

    // equality
    // student exercise
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression equal = relation();
        if(isEqualityOp()){
            Operator operator = new Operator(match(token.type()));
            // because choice
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
        //int
        if(token.type().equals(TokenType.IntLiteral)){
            value = new IntValue(Integer.parseInt(data));
            token = lexer.next();
        }
        // float
        else if(token.type().equals(TokenType.FloatLiteral)){
            value = new FloatValue(Float.parseFloat(data));
            token = lexer.next();
        }
        // char
        else if(token.type().equals(TokenType.CharLiteral)){
            value = new CharValue(data.charAt(0));
            token = lexer.next();
        }
        //boolean
        else if(token.type().equals(TokenType.True)){
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

    // is Add operation
    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
                token.type().equals(TokenType.Minus);
    }

    // is Multiply operation
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
                token.type().equals(TokenType.Divide);
    }

    // is Unary operation
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
                token.type().equals(TokenType.Minus);
    }

    // is Equality operation
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
                token.type().equals(TokenType.NotEqual);
    }

    // is Relational operation
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
                token.type().equals(TokenType.LessEqual) ||
                token.type().equals(TokenType.Greater) ||
                token.type().equals(TokenType.GreaterEqual);
    }

    // is Type operation
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
                || token.type().equals(TokenType.Bool)
                || token.type().equals(TokenType.Float)
                || token.type().equals(TokenType.Char);
    }

    //is Literal operation
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
                isBooleanLiteral() ||
                token.type().equals(TokenType.FloatLiteral) ||
                token.type().equals(TokenType.CharLiteral);
    }

    //is Boolean
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
                token.type().equals(TokenType.False);
    }

    //main
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer("test.txt"));
        Program prog = parser.program();
        prog.display(1);           // display abstract syntax tree student
    }

} // Parser

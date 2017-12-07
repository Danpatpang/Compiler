import java.io.*;
//Lexer
public class Lexer {
    private boolean isEof = false;
    private char ch = ' ';
    private BufferedReader input;
    private String line = "";
    // Line number
    private int lineno = 0;
    // Locate text
    private int col = 1;
    private final String letters = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    // Line break
    private final char eolnCh = '\n';
    // white space
    private final char eofCh = '\004';

    // Lexer constructor
    // read filename, store buffer
    public Lexer (String fileName) { // source filename
        try {
            input = new BufferedReader (new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    // get nextChar
    private char nextChar() {
        // ch == white space
        if (ch == eofCh){
            error("Attempt to read past end of file");
        }
        col++;
        // read nextLine
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            // is nextLine file end
            if (line == null){
                line = "" + eofCh;
            }
            // linenum + content + \n output
            else {
                //System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;        // reset
        } // if col
        // return next char
        return line.charAt(col);
    }

    // Token determine
    public Token next( ) { // Return next token
        do {
            if (isLetter(ch)) { // ident or keyword
                String spelling = concat(letters + digits);
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int or float literal
                String number = concat(digits);
                // int Literal
                if (ch != '.'){
                    return Token.mkIntLiteral(number);
                }
                // float Literal
                number += concat(digits);
                return Token.mkFloatLiteral(number);
            } else switch (ch) {
                case ' ': case '\t': case '\r': case eolnCh:
                    ch = nextChar();
                    break;



                case '/':  // divide or comment
                    ch = nextChar();
                    // divide
                    if (ch != '/')  return Token.divideTok;
                    // comment until next char is linebreak..
                    do {
                        ch = nextChar();
                    } while (ch != eolnCh);
                    ch = nextChar();
                    break;

                case '\'':  // char literal
                    char ch1 = nextChar();
                    nextChar(); // get '
                    ch = nextChar();
                    return Token.mkCharLiteral("" + ch1);


                case eofCh:
                    return Token.eofTok;
                case '+': ch = nextChar();
                    return Token.plusTok;
                case '-': ch = nextChar();
                    return Token.minusTok;
                case '*': ch = nextChar();
                    return Token.multiplyTok;
                case '(': ch = nextChar();
                    return Token.leftParenTok;
                case ')': ch = nextChar();
                    return Token.rightParenTok;
                case '{': ch = nextChar();
                    return Token.leftBraceTok;
                case '}': ch = nextChar();
                    return Token.rightBraceTok;
                case ';': ch = nextChar();
                    return Token.semicolonTok;
                case ',': ch = nextChar();
                    return Token.commaTok;
                // - * ( ) { } ; ,  student exercise

                case '&':
                    check('&');
                    return Token.andTok;
                case '|':
                    check('|');
                    return Token.orTok;

                case '=':
                    return chkOpt('=', Token.assignTok,
                            Token.eqeqTok);
                case '<':
                    return chkOpt('=', Token.ltTok, Token.lteqTok);
                case '>':
                    return chkOpt('=', Token.gtTok, Token.gteqTok);
                case '!':
                    return chkOpt('=', Token.notTok, Token.noteqTok);
                // < > !  student exercise

                default:  error("Illegal character " + ch);
            } // switch
        } while (true);
    } // next

    // isletter
    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z');
    }
    // isdigit
    private boolean isDigit(char c) {
        return (c>='0' && c<='9');  // student exercise
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c)
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
        ch = nextChar();
        if(ch != c){
            return one;
        }
        ch = nextChar();
        return two;
        // student exercise
    }
    //concat set = letters + digits
    private String concat(String set) {
        String r = "";
        do {
            r += ch;
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);     //until not digit or letter
        return r;
    }

    // error message
    // line output, locate num error
    public void error (String msg) {
        //out | err out have buffer and can change file
        //but err dont have buffer and cant chagne file
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

    static public void main ( String[] argv ) {
        Lexer lexer = new Lexer("test.txt");
        Token tok = lexer.next( );
        while (tok != Token.eofTok) {
            System.out.println(tok.toString());
            tok = lexer.next( );
        }
    } // main

}


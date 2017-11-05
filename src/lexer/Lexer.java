package lexer;

import java.io.*;
import token.*;

//Lexer
public class Lexer {
    private boolean isEof = false;
    private char ch = ' ';
    private BufferedReader input;
    private String line = "";
    private int lineno = 0;
    // 몇 번째 글자인지 파악
    private int col = 1;
    private final String letters = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    private final char eolnCh = '\n';   //줄바꿈
    private final char eofCh = '\004';  //공백

    // Lexer 생성자
    // filename을 읽어서 버퍼에 저장
    public Lexer (String fileName) { // source filename
        try {
            input = new BufferedReader (new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    // 다음 글자 받아오기
    private char nextChar() {
        // ch가 공백일 경우
        if (ch == eofCh){
            error("Attempt to read past end of file");
        }
        col++;
        // col이 line길이의 이상이 되면 다음 라인 읽기
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            // 다음 라인이 파일의 끝인 경우
            if (line == null){
                line = "" + eofCh;
            }
            // 라인 넘버 + 내용 + \n 출력
            else {
                System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;        // 초기화
        } // if col
        // 다음 글자 반환
        return line.charAt(col);
    }

    // Token 결정
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
                    // 나눗셈의 경우
                    if (ch != '/')  return Token.divideTok;
                    // 주석의 경우 줄바꿈이 나올 때까지 다음 글자 읽기
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

    // 문자일 경우
    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z');
    }
    // 숫자일 경우
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
    //합치기 set = letters + digits
    private String concat(String set) {
        String r = "";
        do {
            r += ch;
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);     //문자, 숫자가 아닐 때까지 실행 후 반환
        return r;
    }

    // error message 출력
    // line 출력, 몇 번째 글자에서 error 출력 및 종료
    public void error (String msg) {
        //out의 경우 결과를 다른 파일로 redirect 가능하지만, err은 불가 but 같은 출력문
        //out은 버퍼가 있고, err은 버퍼가 없다.
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

    static public void main ( String[] argv ) {
        Lexer lexer = new Lexer(argv[0]);
        Token tok = lexer.next( );
        while (tok != Token.eofTok) {
            System.out.println(tok.toString());
            tok = lexer.next( );
        }
    } // main

}


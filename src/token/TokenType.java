// token type

public enum TokenType {
     Bool, Char, Else, False, Float,
     If, Int, Main, True, While,
     Eof, LeftBrace, RightBrace, LeftBracket, RightBracket,
     LeftParen, RightParen, Semicolon, Comma, Assign,
     Equals, Less, LessEqual, Greater, GreaterEqual,
     Not, NotEqual, Plus, Minus, Multiply,
     Divide, And, Or, Identifier, IntLiteral,
     FloatLiteral, CharLiteral;

     static public void main ( String[] argv ) {
          System.out.print(TokenType.Bool.compareTo(TokenType.Eof));
     }
}
### sudo yum install flex
### sudo yum install bison
### lex postfix_operators.l
### yacc -d postfix_operators.y
### gcc lex.yy.c y.tab.c -ll -ly -o postfix_operators

### ./postfix_operators

%{
extern int yylval;
#include "y.tab.h"
%}

%%

[0-9]+ {yylval = atoi(yytext); return INTEGER;}
[\t\n] {return 0;}
.	{return *yytext;}
%%

int yywrap(void) {
return 1;
}

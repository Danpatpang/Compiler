%{
#include<stdio.h>
%}

%token INTEGER
%left '+' '-'
%left '*' '/'

%%
E : E '+' T {printf(" +");}
	| E '-' T {printf(" -");}
	| T
;

T : T '*' F {printf(" *");}
	| T'/' F {printf(" /");}
	| F;

F : '(' E ')' | INTEGER {printf(" %d",yylval);};
%%

int main(){
yyparse();
}

int yyerror (char *msg) {
	return printf("error YACC: %s\n", msg);
}
